import { TftRecentMatchDto, TftBadgeDto } from "@/lib/types";
import { dataDragonService } from "@/lib/dataDragon";
import { getChampionData, getItemData, getTraitData } from "@/lib/tftData";
import Image from "next/image";
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
  TooltipProvider,
} from "@/components/ui/tooltip";
import { useState, useEffect, useCallback } from "react";
import { cn } from "@/lib/utils";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { TftBadgeCard } from "@/components/tft/TftBadgeCard";
import { findBadgesFromPuuids } from "@/lib/api";

const RARITY_COLORS = {
  0: "border-gray-400 text-gray-400", // 1비용
  1: "border-green-500 text-green-500", // 2비용
  2: "border-blue-500 text-blue-500", // 3비용
  4: "border-purple-600 text-purple-600", // 4비용
  6: "border-yellow-400 text-yellow-400", // 5비용
} as const;

const TRAIT_STYLE_COLORS = {
  1: "bg-amber-700", // Bronze
  2: "bg-gray-600", // Silver
  3: "bg-yellow-600", // Gold
  4: "bg-purple-700", // Chromatic
} as const;

const PLACEMENT_COLORS = {
  1: "bg-yellow-100", // 금
  2: "bg-gray-200", // 은
  3: "bg-amber-100", // 동
  4: "bg-amber-100", // 동
  5: "bg-gray-100", // 회색
  6: "bg-gray-100",
  7: "bg-gray-100",
  8: "bg-gray-100",
} as const;

interface UnitProps {
  unit: TftRecentMatchDto["units"][0];
  isSelected?: boolean;
  onUnitClick: (characterId: string) => void;
}

const Unit = ({
  unit,
  isSelected,
  onUnitClick,
  scale = 1,
}: UnitProps & { scale?: number }) => {
  const stars = "★".repeat(unit.tier);
  const rarityColor =
    RARITY_COLORS[unit.rarity as keyof typeof RARITY_COLORS] ||
    RARITY_COLORS[0];
  const [borderColor] = rarityColor.split(" ");

  const size = Math.floor(48 * scale);
  const itemSize = Math.floor(15 * scale);

  // Get static data directly
  const championData = getChampionData(unit.character_id);
  const itemsData = unit.itemNames.map((itemId) => getItemData(itemId));

  return (
    <div
      className={`flex flex-col items-center p-${
        scale === 1 ? "1" : "0.5"
      } cursor-pointer transition-all`}
      style={{ width: `${size}px` }}
      onClick={() => onUnitClick(unit.character_id)}
    >
      <div
        className={`text-center text-xs leading-none ${
          rarityColor.split(" ")[1]
        }`}
      >
        {stars}
      </div>
      <Tooltip>
        <TooltipTrigger asChild>
          <div
            className="relative my-0.5"
            style={{ width: `${size}px`, height: `${size}px` }}
          >
            <Image
              src={
                dataDragonService.getChampionImage(unit.character_id) ||
                "/placeholder.svg"
              }
              alt={unit.name}
              fill
              sizes="(max-width: 48px) 100vw"
              className={`object-cover rounded-sm ${borderColor} border-2 ${
                isSelected ? "ring-2 ring-blue-400" : ""
              } object-[80%_30%]`}
            />
          </div>
        </TooltipTrigger>
        <TooltipContent>
          <p className="font-medium text-sm">
            {championData?.name ||
              dataDragonService.getChampionName(unit.character_id)}
          </p>
          {championData?.ability?.modifiedDesc && (
            <p className="text-sm text-gray-600 mt-1 max-w-[300px] max-h-[200px] overflow-y-auto">
              {championData.ability.modifiedDesc}
            </p>
          )}
        </TooltipContent>
      </Tooltip>
      {unit.itemNames.length > 0 && (
        <div className="flex justify-center items-center gap-[2px] w-full">
          {unit.itemNames.slice(0, 3).map((itemId, idx) => {
            const itemImageUrl = dataDragonService.getItemImage(itemId);
            if (!itemImageUrl) return null;

            return (
              <Tooltip key={idx}>
                <TooltipTrigger asChild>
                  <div
                    className="relative flex-shrink-0"
                    style={{ width: `${itemSize}px`, height: `${itemSize}px` }}
                  >
                    <Image
                      src={itemImageUrl || "/placeholder.svg"}
                      alt={dataDragonService.getItemName(itemId) || itemId}
                      fill
                      sizes="(max-width: 15px) 100vw"
                      className="object-cover rounded-sm"
                    />
                  </div>
                </TooltipTrigger>
                <TooltipContent>
                  <div className="max-w-[300px]">
                    <p className="font-medium text-sm">
                      {dataDragonService.getItemName(itemId)}
                    </p>
                    {itemsData[idx]?.modifiedDesc && (
                      <p className="text-sm text-gray-600 mt-1 max-h-[200px] overflow-y-auto">
                        {itemsData[idx].modifiedDesc}
                      </p>
                    )}
                  </div>
                </TooltipContent>
              </Tooltip>
            );
          })}
        </div>
      )}
    </div>
  );
};

interface TraitDisplayProps {
  trait: TftRecentMatchDto["traits"][0];
  scale?: number;
}

const TraitDisplay = ({ trait, scale = 1 }: TraitDisplayProps) => {
  // 현재 세트의 trait 데이터를 가져옴
  const traitData = getTraitData(trait.name);
  const displayName = dataDragonService.getTraitName(trait.name) || trait.name;

  // trait 데이터가 없거나 tier가 0이면 표시하지 않음
  if (!traitData || !trait.tier_current || trait.style === 0) return null;

  const styleColor =
    TRAIT_STYLE_COLORS[trait.style as keyof typeof TRAIT_STYLE_COLORS];

  const iconSize = Math.floor(20 * scale);
  const fontSize = scale === 1 ? "text-sm" : "text-xs";
  const padding = scale === 1 ? "px-2 py-1" : "px-1.5 py-0.5";

  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <div
          className={cn(
            "flex items-center gap-1.5 rounded-md text-white font-medium",
            fontSize,
            padding,
            styleColor
          )}
        >
          <div
            className="relative"
            style={{ width: `${iconSize}px`, height: `${iconSize}px` }}
          >
            <Image
              src={dataDragonService.getTraitImage(trait.name)}
              alt={displayName}
              width={iconSize}
              height={iconSize}
              className="object-contain"
            />
          </div>
          <span>{displayName}</span>
        </div>
      </TooltipTrigger>
      <TooltipContent>
        <div className="max-w-[300px]">
          <p className="font-medium text-sm mb-1">{displayName}</p>
          {traitData.modifiedDesc && (
            <p className="text-sm text-gray-600 max-h-[200px] overflow-y-auto">
              {traitData.modifiedDesc}
            </p>
          )}
          <p className="text-sm text-gray-500 mt-1">
            활성화된 유닛 수: {trait.num_units}
          </p>
        </div>
      </TooltipContent>
    </Tooltip>
  );
};

interface ParticipantRowProps {
  participant: TftRecentMatchDto["participants"][0];
  showBadgesOnLeft?: boolean;
  scale?: number;
  badges?: TftBadgeDto[];
}

const ParticipantRow = ({
  participant,
  showBadgesOnLeft = true,
  scale = 1,
  badges,
}: ParticipantRowProps) => {
  const [selectedUnit, setSelectedUnit] = useState<string | null>(null);

  const badgeDisplay =
    badges && badges.length > 0 ? (
      <TftBadgeCard
        riotIdGameName={participant.riotIdGameName}
        riotIdTagline={participant.riotIdTagline}
        isCompact={true}
        afterMatchStatus={false}
        badges={badges}
      />
    ) : null;

  const handleUnitClick = (characterId: string) => {
    setSelectedUnit(selectedUnit === characterId ? null : characterId);
  };

  // Sort traits by style (descending) and then alphabetically
  const sortedTraits = [...participant.traits]
    .filter((trait) => trait.tier_current > 0)
    .sort((a, b) => {
      if (b.style !== a.style) return b.style - a.style;
      return (dataDragonService.getTraitName(a.name) || a.name).localeCompare(
        dataDragonService.getTraitName(b.name) || b.name
      );
    });

  // Calculate scale based on unit count but maintain consistent card size
  const unitScale = participant.units.length >= 11 ? 0.75 : scale;
  // Base height for trait row stays constant regardless of unit scale
  const traitRowHeight = Math.floor(32 * scale); // Use original scale for consistent card height

  return (
    <div className="bg-slate-800/50 border border-slate-700/50 rounded-lg overflow-hidden hover:border-indigo-500/50 transition-all">
      <div className="flex items-center justify-between p-3">
        <div
          className={cn(
            "flex items-center gap-2",
            participant.placement <= 4 ? "text-indigo-400" : "text-gray-400"
          )}
        >
          {showBadgesOnLeft && badgeDisplay}
          <span className="font-medium text-white">
            {participant.riotIdGameName}
            <span className="text-gray-400">#{participant.riotIdTagline}</span>
          </span>
          {!showBadgesOnLeft && badgeDisplay}
        </div>
        <div className="flex items-center gap-2 text-sm">
          <Badge
            variant={participant.placement <= 4 ? "default" : "secondary"}
            className={cn(
              "font-medium text-white",
              participant.placement <= 4
                ? "bg-indigo-500/80 hover:bg-indigo-500"
                : "bg-slate-700/50 hover:bg-slate-700"
            )}
          >
            {participant.placement}등
          </Badge>
          <span className="text-gray-400">Lv.{participant.level}</span>
        </div>
      </div>

      <div className="px-3 pb-3">
        {/* Traits container with fixed height */}
        <div
          className="flex flex-wrap content-end gap-1.5 mb-3 overflow-hidden"
          style={{ height: `${traitRowHeight * 2}px` }}
        >
          {sortedTraits.map((trait, index) => (
            <TraitDisplay key={index} trait={trait} scale={unitScale} />
          ))}
        </div>

        {/* Units container with dynamic scale but fixed container size */}
        <div
          className={cn(
            "flex flex-wrap gap-1.5",
            participant.units.length >= 11 ? "justify-start" : ""
          )}
        >
          {participant.units.map((unit, index) => (
            <Unit
              key={index}
              unit={unit}
              isSelected={selectedUnit === unit.character_id}
              onUnitClick={handleUnitClick}
              scale={unitScale}
            />
          ))}
        </div>
      </div>
    </div>
  );
};

interface TftMatchCardProps {
  match: TftRecentMatchDto;
}

const globalCache = {
  champions: new Map(),
  traits: new Map(),
  items: new Map(),
};

export default function TftMatchCard({ match }: TftMatchCardProps) {
  const [showParticipants, setShowParticipants] = useState(false);
  const [selectedUnit, setSelectedUnit] = useState<string | null>(null);
  const [isLoadingBadges, setIsLoadingBadges] = useState(false);
  const [badgeError, setBadgeError] = useState<string | null>(null);
  const [participantBadges, setParticipantBadges] = useState<{
    [puuid: string]: TftBadgeDto[];
  }>({});

  // 배지 데이터 가져오기
  const fetchBadgesData = async () => {
    setIsLoadingBadges(true);
    setBadgeError(null);
    try {
      const puuids = match.participants.map((p) => p.puuid);
      const uniquePuuids = Array.from(new Set(puuids));

      const fetchedBadges = await findBadgesFromPuuids(uniquePuuids);

      setParticipantBadges(fetchedBadges);
    } catch (error) {
      console.error("[TftMatchCard] Failed to fetch badges:", error);
      setBadgeError("배지 정보를 불러오는데 실패했습니다.");
    } finally {
      setIsLoadingBadges(false);
    }
  };

  // 정보 보기 버튼 클릭 핸들러
  const handleShowParticipants = () => {
    const newShowParticipants = !showParticipants;
    setShowParticipants(newShowParticipants);

    // 정보를 보여줄 때만 배지 데이터를 가져옴
    if (newShowParticipants && Object.keys(participantBadges).length === 0) {
      fetchBadgesData();
    }
  };

  const formatGameLength = (seconds: number) => {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = Math.floor(seconds % 60);
    return `${minutes}:${remainingSeconds.toString().padStart(2, "0")}`;
  };

  const formatTimeAgo = (timestamp: number) => {
    const now = Date.now();
    const diffInMinutes = Math.floor((now - timestamp) / (1000 * 60));

    if (diffInMinutes < 60) {
      return `${diffInMinutes}분 전`;
    }

    const diffInHours = Math.floor(diffInMinutes / 60);
    if (diffInHours < 24) {
      return `${diffInHours}시간 전`;
    }

    const diffInDays = Math.floor(diffInHours / 24);
    if (diffInDays < 7) {
      return `${diffInDays}일 전`;
    }

    const diffInWeeks = Math.floor(diffInDays / 7);
    return `${diffInWeeks}주 전`;
  };

  const handleUnitClick = (characterId: string) => {
    setSelectedUnit(selectedUnit === characterId ? null : characterId);
  };

  // Sort traits by style (descending) and then alphabetically
  const sortedTraits = [...match.traits]
    .filter((trait) => trait.tier_current > 0)
    .sort((a, b) => {
      if (b.style !== a.style) return b.style - a.style;
      return (dataDragonService.getTraitName(a.name) || a.name).localeCompare(
        dataDragonService.getTraitName(b.name) || b.name
      );
    });

  // Sort participants by placement
  const sortedParticipants = [...match.participants].sort(
    (a, b) => a.placement - b.placement
  );
  const leftParticipants = sortedParticipants.slice(0, 4);
  const rightParticipants = sortedParticipants.slice(4, 8);

  return (
    <div className="bg-slate-800/50 border border-slate-700/50 rounded-lg overflow-hidden hover:border-indigo-500/50 transition-all">
      <div className="flex">
        {/* Match Info - Left Side */}
        <div
          className={cn(
            "flex flex-col justify-between px-4 py-3 min-w-[140px] border-r border-slate-700/50",
            match.placement <= 4 ? "bg-indigo-500/10" : "bg-slate-700/10"
          )}
        >
          <div>
            <div className="flex items-baseline gap-2">
              <Badge
                variant={match.placement <= 4 ? "default" : "secondary"}
                className={cn(
                  "text-lg font-medium px-3 py-1",
                  match.placement <= 4
                    ? "bg-indigo-500/80 hover:bg-indigo-500"
                    : "bg-slate-700/50 hover:bg-slate-700"
                )}
              >
                {match.placement}등
              </Badge>
              <div className="text-sm text-gray-400">
                {formatTimeAgo(match.gameCreation)}
              </div>
            </div>
            <div className="text-sm text-gray-300 mt-2">
              {formatGameLength(match.gameLength)} • Lv.{match.level}
            </div>
            <Badge
              variant="outline"
              className="mt-2 text-xs border-slate-600 text-gray-400"
            >
              {match.queueType === "standard" ? "일반 랭크" : match.queueType}
            </Badge>
          </div>

          <Button
            variant="ghost"
            size="sm"
            onClick={handleShowParticipants}
            className="mt-2 text-gray-400 hover:text-white hover:bg-slate-700/50"
          >
            {showParticipants ? "정보 숨기기" : "정보 보기"}
          </Button>
        </div>

        {/* Units and Traits - Right Side */}
        <div className="flex-1">
          {/* Traits */}
          <div className="px-4 pt-3">
            <div className="flex flex-wrap gap-1.5">
              {sortedTraits.map((trait, index) => (
                <TraitDisplay key={index} trait={trait} />
              ))}
            </div>
          </div>

          {/* Units */}
          <div className="px-4 pt-2 pb-3">
            <div className="flex flex-wrap gap-1.5">
              {match.units.map((unit, index) => (
                <Unit
                  key={index}
                  unit={unit}
                  isSelected={selectedUnit === unit.character_id}
                  onUnitClick={handleUnitClick}
                />
              ))}
            </div>
          </div>
        </div>
      </div>

      {showParticipants && (
        <div className="border-t border-slate-700/50">
          {isLoadingBadges ? (
            <div className="p-4 text-center text-gray-400">배지 로딩 중...</div>
          ) : badgeError ? (
            <div className="p-4 text-center text-red-400">{badgeError}</div>
          ) : Object.keys(participantBadges).length > 0 ? (
            <div className="grid grid-cols-2 gap-4 p-4 bg-slate-900/30">
              <div className="space-y-4">
                {leftParticipants.map((participant) => {
                  const participantBadgeData =
                    participantBadges[participant.puuid] || [];
                  return (
                    <ParticipantRow
                      key={participant.puuid}
                      participant={participant}
                      showBadgesOnLeft={true}
                      scale={0.8}
                      badges={participantBadgeData}
                    />
                  );
                })}
              </div>
              <div className="space-y-4">
                {rightParticipants.map((participant) => {
                  const participantBadgeData =
                    participantBadges[participant.puuid] || [];
                  return (
                    <ParticipantRow
                      key={participant.puuid}
                      participant={participant}
                      showBadgesOnLeft={false}
                      scale={0.8}
                      badges={participantBadgeData}
                    />
                  );
                })}
              </div>
            </div>
          ) : null}
        </div>
      )}
    </div>
  );
}
