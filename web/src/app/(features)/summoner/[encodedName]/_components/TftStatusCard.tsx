"use client";

import { useState, useEffect } from "react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import {
  Tooltip,
  TooltipProvider,
  TooltipTrigger,
  TooltipContent,
} from "@/components/ui/tooltip";
import { Crown, Star, Trophy } from "lucide-react";
import { getTftStatus } from "@/lib/api";
import type { RiotAccountDto, TftStatusDto } from "@/lib/types";
import { TftBadgeCard } from "@/components/tft/TftBadgeCard";
import { useSummonerPageLoadStore } from "@/store/summonerPageStore";

type TftStatusCardProps = {
  account: RiotAccountDto;
  selectedQueueType: string;
  formatQueueType: (queueType: string) => string;
};

export function TftStatusCard({
  account,
  selectedQueueType,
  formatQueueType,
}: TftStatusCardProps) {
  const { accountStatus, statusStatus, setStatusStatus } =
    useSummonerPageLoadStore();
  const [statuses, setStatuses] = useState<TftStatusDto[]>([]);

  useEffect(() => {
    const fetchData = async () => {
      if (accountStatus !== "success") return;
      try {
        setStatusStatus("loading");
        const statusData = await getTftStatus(
          account.gameName,
          account.tagLine
        );
        setStatuses(statusData);
        setStatusStatus("success");
      } catch (error) {
        console.error("Failed to fetch status/badge data:", error);
        setStatusStatus("error");
      }
    };

    fetchData();
  }, [accountStatus, account.gameName, account.tagLine, setStatusStatus]);

  const getCurrentTftStatus = () => {
    if (!statuses || statuses.length === 0) return null;
    const status = statuses.find(
      (status) => status.queueType === selectedQueueType
    );
    return status && status.tier ? status : null;
  };

  const currentStatus = getCurrentTftStatus();

  if (statusStatus === "loading" || statusStatus === "idle") {
    return (
      <Card className="bg-slate-800/50 border-slate-700/50">
        <CardHeader>
          <CardTitle className="text-xl text-white">랭크 정보</CardTitle>
        </CardHeader>
        <CardContent className="h-[200px] flex items-center justify-center">
          <div className="w-8 h-8 border-2 border-indigo-500 border-t-transparent rounded-full animate-spin" />
        </CardContent>
      </Card>
    );
  }

  if (!currentStatus) {
    return (
      <Card className="bg-slate-800/50 border-slate-700/50 hover:border-indigo-500/50 transition-colors">
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="text-lg text-white flex items-center gap-2">
                <div className="w-8 h-8 bg-gradient-to-br from-slate-600 to-indigo-600 rounded-lg flex items-center justify-center">
                  <Crown className="w-5 h-5 text-white" />
                </div>
                {formatQueueType(selectedQueueType)}
              </CardTitle>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <div className="py-4 text-center">
            <div className="w-12 h-12 bg-slate-700/30 rounded-full flex items-center justify-center mx-auto mb-2">
              <Trophy className="w-6 h-6 text-slate-500" />
            </div>
            <h3 className="text-lg font-semibold text-slate-300 mb-1">
              아직 기록이 없어요!
            </h3>
            <p className="text-xs text-slate-400">
              TFT에서 {formatQueueType(selectedQueueType)} 모드를 플레이하고
              <br />
              새로운 기록을 만들어보세요.
            </p>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className="bg-slate-800/50 border-slate-700/50 hover:border-indigo-500/50 transition-colors">
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle className="text-lg text-white flex items-center gap-2">
              <div className="w-8 h-8 bg-gradient-to-br from-slate-600 to-indigo-600 rounded-lg flex items-center justify-center">
                <Crown className="w-5 h-5 text-white" />
              </div>
              {formatQueueType(selectedQueueType)}
            </CardTitle>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        <div className="space-y-2">
          <div className="flex items-center gap-2">
            <span className="text-lg font-bold text-white">
              {currentStatus.tier} {currentStatus.rank}
            </span>
            <span className="text-gray-300">
              {currentStatus.leaguePoints} LP
            </span>
            {currentStatus.hotStreak && (
              <Badge className="bg-gradient-to-r from-orange-500 to-red-500 text-white">
                <Star className="w-3 h-3 mr-1" />
                연승
              </Badge>
            )}
          </div>

          <div className="flex flex-wrap gap-2 mt-2">
            <TftBadgeCard
              riotIdGameName={account.gameName}
              riotIdTagline={account.tagLine}
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div className="text-center p-2 rounded-lg bg-green-500/10 border border-green-500/30">
              <div className="text-lg font-bold text-green-400">
                {currentStatus.wins}
              </div>
              <div className="text-xs text-gray-400">승리</div>
            </div>
            <div className="text-center p-2 rounded-lg bg-red-500/10 border border-red-500/30">
              <div className="text-lg font-bold text-red-400">
                {currentStatus.losses}
              </div>
              <div className="text-xs text-gray-400">패배</div>
            </div>
          </div>

          <div className="space-y-2 pt-1 border-t border-slate-700">
            <div className="flex justify-between">
              <span className="text-sm text-gray-400">승률</span>
              <span className="text-sm text-white">
                {currentStatus.wins + currentStatus.losses > 0
                  ? (
                      (currentStatus.wins /
                        (currentStatus.wins + currentStatus.losses)) *
                      100
                    ).toFixed(1)
                  : "0.0"}
                %
              </span>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
