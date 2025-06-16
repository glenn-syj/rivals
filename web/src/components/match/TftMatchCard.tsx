import { TftRecentMatchDto } from "@/lib/types";
import { dataDragonService } from "@/lib/dataDragon";
import { getChampionData, getItemData, getTraitData } from "@/lib/tftData";
import Image from "next/image";
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import { useState, useEffect, useCallback } from "react";
import { getTftBadges } from "@/lib/api";

const BADGE_EMOJIS = {
  LUXURY: "💎",
  DAMAGE_DEALER: "💥",
  EXECUTOR: "🎯",
  MVP: "🥇",
  STEADY: "📈",
} as const;

const RARITY_COLORS = {
  0: "border-gray-400 text-gray-400", // 1비용
  1: "border-green-500 text-green-500", // 2비용
  2: "border-blue-500 text-blue-500", // 3비용
  4: "border-purple-600 text-purple-600", // 4비용
  6: "border-yellow-400 text-yellow-400", // 5비용
} as const;

const TRAIT_STYLE_COLORS = {
  1: "bg-amber-700/80", // Bronze
  2: "bg-gray-400/80", // Silver
  3: "bg-yellow-500/80", // Gold
  4: "bg-purple-600/80", // Chromatic
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
              src={dataDragonService.getChampionImage(unit.character_id)}
              alt={unit.name}
              fill
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
                      src={itemImageUrl}
                      alt={dataDragonService.getItemName(itemId) || itemId}
                      fill
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
          className={`flex items-center gap-1.5 rounded-md ${fontSize} ${padding} ${styleColor}`}
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
}

const ParticipantRow = ({
  participant,
  showBadgesOnLeft = true,
  scale = 1,
}: ParticipantRowProps) => {
  const [badges, setBadges] = useState<string[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [selectedUnit, setSelectedUnit] = useState<string | null>(null);

  const loadBadges = useCallback(async () => {
    // 이미 배지가 로드되어 있다면 다시 로드하지 않음
    if (badges.length > 0) return;

    setIsLoading(true);
    try {
      const badgeData = await getTftBadges(
        participant.riotIdGameName,
        participant.riotIdTagline
      );
      setBadges(badgeData.map((badge) => badge.badgeType));
    } catch (error) {
      console.error("Failed to load badges:", error);
    } finally {
      setIsLoading(false);
    }
  }, [participant.riotIdGameName, participant.riotIdTagline, badges.length]); // isLoading 제거

  useEffect(() => {
    loadBadges();
  }, [loadBadges]);

  const badgeDisplay = (
    <div className="flex gap-1">
      {isLoading ? (
        <span className="text-gray-400">로딩중...</span>
      ) : badges.length > 0 ? (
        badges.map((badgeType, index) => (
          <span key={index} className="text-lg">
            {BADGE_EMOJIS[badgeType as keyof typeof BADGE_EMOJIS]}
          </span>
        ))
      ) : (
        <span className="text-gray-400">-</span>
      )}
    </div>
  );

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

  // Calculate height based on scale
  const traitRowHeight = Math.floor(32 * scale); // Base height for trait row

  return (
    <div className="border rounded-lg overflow-hidden bg-white">
      <div className="flex items-center justify-between p-2">
        <div
          className={`flex items-center gap-2 ${
            participant.placement <= 4 ? "text-blue-600" : "text-gray-600"
          }`}
        >
          {showBadgesOnLeft && badgeDisplay}
          <span className="font-medium">
            {participant.riotIdGameName}#{participant.riotIdTagline}
          </span>
          {!showBadgesOnLeft && badgeDisplay}
        </div>
        <div className="text-sm text-gray-600">
          <span className="font-medium">{participant.placement}등</span>
          <span className="mx-1">•</span>
          <span>Lv.{participant.level}</span>
        </div>
      </div>

      <div className="px-2 pb-2">
        {/* Traits - Fixed height container for 2 rows */}
        <div
          className="flex flex-wrap content-start gap-1 mb-2 overflow-hidden"
          style={{ height: `${traitRowHeight * 2}px` }}
        >
          {sortedTraits.map((trait, index) => (
            <TraitDisplay key={index} trait={trait} scale={scale} />
          ))}
        </div>

        {/* Units - Single row */}
        <div className="flex flex-wrap gap-1">
          {participant.units.map((unit, index) => (
            <Unit
              key={index}
              unit={unit}
              isSelected={selectedUnit === unit.character_id}
              onUnitClick={handleUnitClick}
              scale={scale}
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
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const initializeData = async () => {
      await dataDragonService.initialize();
      setIsLoading(false);
    };
    initializeData();
  }, []);

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
    <div className="border rounded-lg overflow-hidden">
      {isLoading ? (
        <div className="p-4 text-center">데이터 로딩중...</div>
      ) : (
        <>
          <div className="flex">
            {/* Match Info - Left Side */}
            <div
              className={`flex flex-col justify-between px-4 py-3 min-w-[140px] ${
                PLACEMENT_COLORS[
                  match.placement as keyof typeof PLACEMENT_COLORS
                ]
              }`}
            >
              <div>
                <div className="flex items-baseline gap-2">
                  <div className="text-lg font-medium">{match.placement}등</div>
                  <div className="text-sm text-gray-600">
                    {formatTimeAgo(match.gameCreation)}
                  </div>
                </div>
                <div className="text-sm mt-1">
                  {formatGameLength(match.gameLength)} • Lv.{match.level}
                </div>
                <div className="text-sm text-gray-600 mt-1">
                  {match.queueType === "standard"
                    ? "일반 랭크"
                    : match.queueType}
                </div>
              </div>

              <button
                onClick={() => setShowParticipants(!showParticipants)}
                className="text-sm text-blue-600 hover:text-blue-800 mt-2"
              >
                {showParticipants ? "정보 숨기기" : "정보 보기"}
              </button>
            </div>

            {/* Units and Traits - Right Side */}
            <div className="flex-1">
              {/* Traits */}
              <div className="px-3 pt-3">
                <div className="flex flex-wrap gap-1.5">
                  {sortedTraits.map((trait, index) => (
                    <TraitDisplay key={index} trait={trait} />
                  ))}
                </div>
              </div>

              {/* Units */}
              <div className="px-3 pt-2 pb-3">
                <div className="flex flex-wrap gap-1">
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
            <div className="border-t">
              <div className="grid grid-cols-2 gap-4 p-4 bg-gray-50">
                <div className="space-y-4">
                  {leftParticipants.map((participant) => (
                    <ParticipantRow
                      key={participant.puuid}
                      participant={participant}
                      showBadgesOnLeft={true}
                      scale={0.8}
                    />
                  ))}
                </div>
                <div className="space-y-4">
                  {rightParticipants.map((participant) => (
                    <ParticipantRow
                      key={participant.puuid}
                      participant={participant}
                      showBadgesOnLeft={false}
                      scale={0.8}
                    />
                  ))}
                </div>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}
