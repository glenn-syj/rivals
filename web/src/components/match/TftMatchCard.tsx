import { TftRecentMatchDto } from "@/lib/types";
import { dataDragonService } from "@/lib/dataDragon";
import Image from "next/image";
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import { useState, useEffect } from "react";

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

const Unit = ({ unit, isSelected, onUnitClick }: UnitProps) => {
  const stars = "★".repeat(unit.tier);
  const rarityColor =
    RARITY_COLORS[unit.rarity as keyof typeof RARITY_COLORS] ||
    RARITY_COLORS[0];
  const [borderColor] = rarityColor.split(" ");

  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <div
          className="flex flex-col items-center p-1 cursor-pointer transition-all w-[48px]"
          onClick={() => onUnitClick(unit.character_id)}
        >
          <div
            className={`text-center text-xs leading-none ${
              rarityColor.split(" ")[1]
            }`}
          >
            {stars}
          </div>
          <div className="relative w-[48px] h-[48px] my-0.5">
            <Image
              src={dataDragonService.getChampionImage(unit.character_id)}
              alt={unit.name}
              fill
              className={`object-cover rounded-sm ${borderColor} border-3 ${
                isSelected ? "ring-2 ring-blue-400" : ""
              } object-[80%_30%]`}
            />
          </div>
          {unit.itemNames.length > 0 && (
            <div className="flex justify-center items-center gap-[2px] w-full">
              {unit.itemNames.slice(0, 3).map((itemId, idx) => (
                <Tooltip key={idx}>
                  <TooltipTrigger asChild>
                    <div className="relative w-[15px] h-[15px] flex-shrink-0">
                      <Image
                        src={dataDragonService.getItemImage(itemId)}
                        alt={dataDragonService.getItemName(itemId) || itemId}
                        fill
                        className="object-cover rounded-sm"
                      />
                    </div>
                  </TooltipTrigger>
                  <TooltipContent>
                    <p className="font-medium text-sm">
                      {dataDragonService.getItemName(itemId)}
                    </p>
                  </TooltipContent>
                </Tooltip>
              ))}
            </div>
          )}
        </div>
      </TooltipTrigger>
      <TooltipContent>
        <p className="font-medium text-sm">
          {dataDragonService.getChampionName(unit.character_id)}
        </p>
      </TooltipContent>
    </Tooltip>
  );
};

interface TraitDisplayProps {
  trait: TftRecentMatchDto["traits"][0];
}

const TraitDisplay = ({ trait }: TraitDisplayProps) => {
  const [traitDescription, setTraitDescription] = useState<string | null>(null);

  useEffect(() => {
    const fetchTraitDescription = async () => {
      try {
        const response = await fetch(`/api/tft?type=traits&id=${trait.name}`);
        const data = await response.json();
        setTraitDescription(data?.desc || null);
      } catch (error) {
        console.error("Error fetching trait description:", error);
      }
    };

    if (trait.tier_current && trait.style !== 0) {
      fetchTraitDescription();
    }
  }, [trait.name, trait.tier_current, trait.style]);

  if (!trait.tier_current || trait.style === 0) return null;

  const traitId = `${trait.name}`;
  const styleColor =
    TRAIT_STYLE_COLORS[trait.style as keyof typeof TRAIT_STYLE_COLORS];

  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <div
          className={`flex items-center gap-1.5 px-2 py-1 rounded-md text-sm ${styleColor}`}
        >
          <div className="relative w-5 h-5">
            <Image
              src={dataDragonService.getTraitImage(traitId)}
              alt={trait.name}
              width={20}
              height={20}
              className="object-contain"
            />
          </div>
          <span className="text-sm">
            {dataDragonService.getTraitName(traitId) || trait.name}
          </span>
        </div>
      </TooltipTrigger>
      <TooltipContent>
        <div className="max-w-md">
          <p className="font-medium text-sm mb-1">
            {dataDragonService.getTraitName(traitId) || trait.name}
          </p>
          {traitDescription && (
            <p className="text-sm text-gray-600">{traitDescription}</p>
          )}
        </div>
      </TooltipContent>
    </Tooltip>
  );
};

interface TftMatchCardProps {
  match: TftRecentMatchDto;
}

export default function TftMatchCard({ match }: TftMatchCardProps) {
  const [selectedUnit, setSelectedUnit] = useState<string | null>(null);

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

  return (
    <div className="border rounded-lg overflow-hidden">
      <div className="flex">
        {/* Match Info - Left Side */}
        <div
          className={`flex flex-col justify-center px-4 py-3 min-w-[140px] ${
            PLACEMENT_COLORS[match.placement as keyof typeof PLACEMENT_COLORS]
          }`}
        >
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
            {match.queueType === "standard" ? "일반 랭크" : match.queueType}
          </div>
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
    </div>
  );
}
