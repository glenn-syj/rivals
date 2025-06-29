"use client";

import { useState, useEffect } from "react";
import {
  Card,
  CardHeader,
  CardTitle,
  CardContent,
  CardDescription,
} from "@/components/ui/card";
import { BarChart3, Clock, Trophy } from "lucide-react";
import TftMatchCard from "@/components/match/TftMatchCard";
import { getTftMatches } from "@/lib/api";
import { dataDragonService } from "@/lib/dataDragon";
import type { RiotAccountDto, TftRecentMatchDto } from "@/lib/types";

type TftMatchHistoryProps = {
  account: RiotAccountDto;
};

export function TftMatchHistory({ account }: TftMatchHistoryProps) {
  const [matches, setMatches] = useState<TftRecentMatchDto[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchMatches = async () => {
      try {
        setIsLoading(true);
        await dataDragonService.initialize(); // Ensure data dragon is ready
        const matchesData = await getTftMatches(
          account.gameName,
          account.tagLine
        );
        setMatches(matchesData);
      } catch (error) {
        console.error("Failed to fetch match history:", error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchMatches();
  }, [account.gameName, account.tagLine]);

  return (
    <Card className="bg-slate-800/50 border-slate-700/50">
      <CardHeader>
        <CardTitle className="text-xl text-white flex items-center gap-3">
          <div className="w-10 h-10 bg-gradient-to-br from-amber-500 to-orange-500 rounded-lg flex items-center justify-center">
            <Trophy className="w-5 h-5 text-white" />
          </div>
          TFT 랭크 전적
        </CardTitle>
        <CardDescription className="text-gray-400">
          최근 게임 기록 및 상세 분석
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          <div className="p-4 rounded-lg bg-slate-700/30 border border-slate-600/50">
            <h3 className="text-lg font-semibold text-white mb-3 flex items-center gap-2">
              <Clock className="w-4 h-4" />
              최근 게임
            </h3>
            {isLoading ? (
              <div className="text-center py-8 text-gray-400">
                <div className="w-8 h-8 border-2 border-indigo-500 border-t-transparent rounded-full animate-spin mx-auto mb-2" />
                <p>매치 기록을 불러오는 중...</p>
              </div>
            ) : matches.length > 0 ? (
              <div className="space-y-4">
                {matches.map((match) => (
                  <TftMatchCard key={match.matchId} match={match} />
                ))}
              </div>
            ) : (
              <div className="text-center py-8 text-gray-400">
                <p>최근 게임 기록이 없습니다</p>
              </div>
            )}
          </div>

          <div className="p-4 rounded-lg bg-slate-700/30 border border-slate-600/50">
            <h3 className="text-lg font-semibold text-white mb-3 flex items-center gap-2">
              <BarChart3 className="w-4 h-4" />
              상세 분석
            </h3>
            <div className="text-center py-8 text-gray-400">
              <p>덱 조합, 아이템 빌드 분석이 여기에 표시됩니다</p>
              <p className="text-sm mt-2">(API 연동 후 구현 예정)</p>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
