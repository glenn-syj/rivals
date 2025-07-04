"use client";

import { useState, useEffect } from "react";
import {
  Tooltip,
  TooltipProvider,
  TooltipTrigger,
  TooltipContent,
} from "@/components/ui/tooltip";
import { getTftBadges } from "@/lib/api";
import type { TftBadgeDto } from "@/lib/types";
import { BADGE_EMOJIS, BADGE_DESCRIPTIONS } from "@/lib/constants";
import { useSummonerPageLoadStore } from "@/store/summonerPageStore";

type TftBadgeCardProps = {
  riotIdGameName: string;
  riotIdTagline: string;
  isCompact?: boolean;
  afterMatchStatus?: boolean;
  badges?: TftBadgeDto[];
};

export function TftBadgeCard({
  riotIdGameName,
  riotIdTagline,
  isCompact = false,
  afterMatchStatus = true,
  badges: propBadges,
}: TftBadgeCardProps) {
  const { matchStatus, badgesStatus, setBadgesStatus } =
    useSummonerPageLoadStore();
  const [badges, setBadges] = useState<TftBadgeDto[]>(propBadges || []);

  useEffect(() => {
    if (propBadges) {
      setBadges(propBadges);
      setBadgesStatus("success");
      return;
    }

    const fetchBadges = async () => {
      if (afterMatchStatus && matchStatus !== "success") return;
      if (!riotIdGameName || !riotIdTagline) return;
      try {
        setBadgesStatus("loading");
        const badgeData = await getTftBadges(riotIdGameName, riotIdTagline);
        setBadges(badgeData);
        setBadgesStatus("success");
      } catch (error) {
        console.error("Failed to fetch badge data:", error);
        setBadgesStatus("error");
      }
    };

    fetchBadges();
  }, [
    matchStatus,
    riotIdGameName,
    riotIdTagline,
    setBadgesStatus,
    afterMatchStatus,
    propBadges,
  ]);

  if (badgesStatus === "loading" || badgesStatus === "idle") {
    return (
      <div className={`text-gray-400 ${isCompact ? "text-xs" : "text-sm"}`}>
        로딩중...
      </div>
    );
  }

  if (badges.length === 0) {
    return (
      <div className={`text-gray-400 ${isCompact ? "text-xs" : "text-sm"}`}>
        -
      </div>
    );
  }

  const badgeContainerClasses = isCompact
    ? "flex gap-1"
    : "flex flex-wrap gap-2";
  const emojiTextSize = isCompact ? "text-lg" : "text-xl";

  return (
    <div className={badgeContainerClasses}>
      {Object.entries(BADGE_EMOJIS).map(([badgeType, emoji]) => {
        const badge = badges.find((b) => b.badgeType === badgeType);
        const hasBadge = !!badge;
        const isActive = badge?.isActive ?? false;

        // Skip rendering if compact and no badge
        if (isCompact && !hasBadge) {
          return null;
        }

        // Simplified rendering logic
        return (
          <TooltipProvider key={badgeType}>
            <Tooltip>
              <TooltipTrigger asChild>
                <div
                  className={`${emojiTextSize} cursor-help ${
                    hasBadge
                      ? isActive
                        ? "opacity-100"
                        : "opacity-30"
                      : "opacity-30"
                  }`}
                >
                  {emoji}
                </div>
              </TooltipTrigger>
              <TooltipContent>
                <p className="font-medium">
                  {
                    BADGE_DESCRIPTIONS[
                      badgeType as keyof typeof BADGE_DESCRIPTIONS
                    ]
                  }
                </p>
                {badge && (
                  <p className="text-sm text-gray-400">
                    진행도: {badge.currentCount}/{badge.requiredCount}
                  </p>
                )}
                {!badge && (
                  <p className="text-sm text-gray-400">배지 정보 없음</p>
                )}
              </TooltipContent>
            </Tooltip>
          </TooltipProvider>
        );
      })}
    </div>
  );
}
