import { TftRecentMatchDto } from "@/lib/types";
import { dataDragonService } from "@/lib/dataDragon";
import Image from "next/image";
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from "@/components/ui/tooltip";

interface UnitProps {
  unit: TftRecentMatchDto["units"][0];
}

const Unit = ({ unit }: UnitProps) => {
  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <div className="border rounded-md p-2">
          <div className="relative w-12 h-12 mx-auto mb-2">
            <Image
              src={dataDragonService.getChampionImage(unit.character_id)}
              alt={unit.name}
              fill
              className="object-cover rounded-md"
            />
            <div
              className={`absolute -bottom-1 -right-1 bg-yellow-500 rounded-full w-5 h-5 flex items-center justify-center text-xs font-bold`}
            >
              {unit.tier}
            </div>
          </div>
          <p className="text-center text-sm font-medium">
            {dataDragonService.getChampionName(unit.character_id) || unit.name}
          </p>
          {unit.itemNames.length > 0 && (
            <div className="flex flex-wrap gap-1 justify-center mt-1">
              {unit.itemNames.map((itemId, idx) => (
                <Tooltip key={idx}>
                  <TooltipTrigger asChild>
                    <div className="relative w-6 h-6">
                      <Image
                        src={dataDragonService.getItemImage(itemId)}
                        alt={dataDragonService.getItemName(itemId) || itemId}
                        fill
                        className="object-cover rounded-md"
                      />
                    </div>
                  </TooltipTrigger>
                  <TooltipContent>
                    <div className="p-2 max-w-xs">
                      <p className="font-bold">
                        {dataDragonService.getItemName(itemId)}
                      </p>
                    </div>
                  </TooltipContent>
                </Tooltip>
              ))}
            </div>
          )}
        </div>
      </TooltipTrigger>
      <TooltipContent>
        <div className="p-2 max-w-xs">
          <p className="font-bold">
            {dataDragonService.getChampionName(unit.character_id)}
          </p>
        </div>
      </TooltipContent>
    </Tooltip>
  );
};

interface TraitDisplayProps {
  trait: TftRecentMatchDto["traits"][0];
}

const TraitDisplay = ({ trait }: TraitDisplayProps) => {
  if (!trait.tier_current) return null;

  const traitId = `${trait.name}`;

  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <div
          className={`flex items-center gap-2 px-2 py-1 rounded-md text-sm ${
            trait.style === 3
              ? "bg-yellow-100"
              : trait.style === 2
              ? "bg-gray-100"
              : "bg-amber-50"
          }`}
        >
          <Image
            src={dataDragonService.getTraitImage(traitId)}
            alt={trait.name}
            width={20}
            height={20}
            className="object-contain"
          />
          <span>
            {dataDragonService.getTraitName(traitId) || trait.name} (
            {trait.tier_current}/{trait.tier_total})
          </span>
        </div>
      </TooltipTrigger>
      <TooltipContent>
        <div className="p-2 max-w-xs">
          <p className="font-bold">
            {dataDragonService.getTraitName(traitId) || trait.name}
          </p>
        </div>
      </TooltipContent>
    </Tooltip>
  );
};

interface TftMatchCardProps {
  match: TftRecentMatchDto;
}

export default function TftMatchCard({ match }: TftMatchCardProps) {
  return (
    <div className="border rounded-md p-4">
      <div className="grid grid-cols-2 gap-4">
        <div>
          <p>
            <span className="font-medium">Match ID:</span> {match.matchId}
          </p>
          <p>
            <span className="font-medium">Game Creation:</span>{" "}
            {new Date(match.gameCreation).toLocaleString()}
          </p>
          <p>
            <span className="font-medium">Game Length:</span>{" "}
            {Math.floor(match.gameLength / 60)}분 {match.gameLength % 60}초
          </p>
          <p>
            <span className="font-medium">Level:</span> {match.level}
          </p>
          <p>
            <span className="font-medium">Placement:</span> {match.placement}
          </p>
          <p>
            <span className="font-medium">Queue Type:</span> {match.queueType}
          </p>
        </div>
      </div>

      <div className="mt-4">
        <p className="font-medium mb-2">Units:</p>
        <div className="grid grid-cols-4 sm:grid-cols-6 md:grid-cols-8 gap-2">
          {match.units.map((unit, index) => (
            <Unit key={index} unit={unit} />
          ))}
        </div>
      </div>

      <div className="mt-4">
        <p className="font-medium mb-2">Traits:</p>
        <div className="flex flex-wrap gap-2">
          {match.traits
            .filter((trait) => trait.tier_current > 0)
            .map((trait, index) => (
              <TraitDisplay key={index} trait={trait} />
            ))}
        </div>
      </div>
    </div>
  );
}
