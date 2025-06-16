// app/(internal)/dev/page.tsx
"use client";

import { useState, useEffect } from "react";
import { getTftMatches } from "@/lib/api";
import { TftRecentMatchDto } from "@/lib/types";
import { dataDragonService } from "@/lib/dataDragon";
import { TftChampion, TftItem } from "@/lib/dataDragonTypes";
import Image from "next/image";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import TftMatchCard from "@/components/match/TftMatchCard";

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
        </div>
      </TooltipTrigger>
      <TooltipContent>
        <p>{unit.name}</p>
      </TooltipContent>
    </Tooltip>
  );
};

interface TraitDisplayProps {
  trait: TftRecentMatchDto["traits"][0];
}

const TraitDisplay = ({ trait }: TraitDisplayProps) => {
  if (!trait.tier_current) return null;

  // trait.name을 ID로 사용하여 "TFTTutorial_" 접두사 추가
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

export default function DevPage() {
  const [gameName, setGameName] = useState("");
  const [tagLine, setTagLine] = useState("");
  const [matches, setMatches] = useState<TftRecentMatchDto[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const initializeData = async () => {
      await dataDragonService.initialize();
    };
    initializeData();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const data = await getTftMatches(gameName, tagLine);
      setMatches(data);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "매치 조회 중 오류가 발생했습니다."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <TooltipProvider>
      <div className="p-8">
        <h1 className="text-2xl font-bold mb-6">TFT Match API Test</h1>

        <form onSubmit={handleSubmit} className="mb-8 space-y-4">
          <div>
            <label className="block text-sm font-medium mb-1">Game Name</label>
            <input
              type="text"
              value={gameName}
              onChange={(e) => setGameName(e.target.value)}
              className="w-full px-3 py-2 border rounded-md"
              placeholder="Enter game name"
            />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">Tag Line</label>
            <input
              type="text"
              value={tagLine}
              onChange={(e) => setTagLine(e.target.value)}
              className="w-full px-3 py-2 border rounded-md"
              placeholder="Enter tag line"
            />
          </div>
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-500 text-white py-2 rounded-md hover:bg-blue-600 disabled:bg-blue-300"
          >
            {loading ? "Loading..." : "Get Matches"}
          </button>
        </form>

        {error && (
          <div className="mb-6 p-4 bg-red-100 text-red-700 rounded-md">
            {error}
          </div>
        )}

        {matches.length > 0 && (
          <div className="space-y-4">
            <h2 className="text-xl font-semibold">Recent Matches</h2>
            {matches.map((match) => (
              <TftMatchCard key={match.id} match={match} />
            ))}
          </div>
        )}
      </div>
    </TooltipProvider>
  );
}
