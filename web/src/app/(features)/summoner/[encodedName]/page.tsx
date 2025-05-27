"use client";

import { useState, useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import { Button } from "@/app/components/ui/button";
import {
  Card,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/app/components/ui/card";
import { Badge } from "@/app/components/ui/badge";
import {
  ArrowLeft,
  Crown,
  Trophy,
  Target,
  Star,
  Users,
  Sword,
  ShoppingCart,
  Search,
} from "lucide-react";
import Link from "next/link";
import { useRivalry } from "@/app/contexts/RivalryContext";
import { mockTftData } from "@/app/lib/mockData";
import { Input } from "@/app/components/ui/input";
import TeamSelectionModal from "@/app/components/TeamSelectionModal";
import type { Player } from "@/app/contexts/RivalryContext";

interface TftStatus {
  tier: string;
  rank: string;
  leaguePoints: number;
  wins: number;
  losses: number;
  hotStreak: boolean;
}

interface RiotAccount {
  puuid: string;
  gameName: string;
  tagLine: string;
}

export default function SummonerPage() {
  const params = useParams();
  const router = useRouter();
  const encodedName = params.encodedName as string;
  const { openRivalryCart, getTotalPlayerCount, addPlayerToTeam } =
    useRivalry();

  const [account, setAccount] = useState<RiotAccount | null>(null);
  const [tftStatus, setTftStatus] = useState<TftStatus | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchInput, setSearchInput] = useState("");
  const [searchError, setSearchError] = useState("");
  const [selectedPlayer, setSelectedPlayer] = useState<Player | null>(null);
  const [isTeamModalOpen, setIsTeamModalOpen] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      if (!encodedName) return;

      const decodedName = decodeURIComponent(encodedName);

      // 목업 데이터에서 조회
      const data = mockTftData[decodedName as keyof typeof mockTftData];

      if (data) {
        setAccount(data.account);
        setTftStatus(data.tftStatus);
      } else {
        setError("소환사를 찾을 수 없습니다");
      }

      setIsLoading(false);
    };

    fetchData();
  }, [encodedName]);

  const handleAddToRivalry = () => {
    if (account) {
      setSelectedPlayer(account);
      setIsTeamModalOpen(true);
    }
  };

  const handleNewSearch = async () => {
    if (!searchInput.trim()) return;

    const parts = searchInput.trim().split("#");
    if (parts.length !== 2) {
      setSearchError("올바른 형식으로 입력해주세요 (예: Hide on bush#KR1)");
      return;
    }

    const searchKey = searchInput.trim();
    if (mockTftData[searchKey as keyof typeof mockTftData]) {
      router.push(`/summoner/${encodeURIComponent(searchInput.trim())}`);
    } else {
      setSearchError("소환사를 찾을 수 없습니다");
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900 flex items-center justify-center">
        <div className="text-center">
          <div className="w-16 h-16 border-4 border-purple-500 border-t-transparent rounded-full animate-spin mx-auto mb-4" />
          <p className="text-white text-lg">전적을 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900 flex items-center justify-center">
        <div className="text-center max-w-md">
          <Card className="bg-red-900/20 border-red-700/50">
            <CardHeader>
              <CardTitle className="text-red-400 flex items-center justify-center">
                <Target className="w-5 h-5 mr-2" />
                오류 발생
              </CardTitle>
              <CardDescription className="text-red-300">
                {error}
              </CardDescription>
              <Button
                onClick={() => router.push("/")}
                variant="outline"
                className="mt-4 border-red-700 text-red-300 hover:bg-red-900/50"
              >
                <ArrowLeft className="w-4 h-4 mr-2" />
                홈으로 돌아가기
              </Button>
            </CardHeader>
          </Card>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900">
      {/* Header */}
      <header className="px-4 lg:px-6 h-16 flex items-center border-b border-purple-800/30 bg-slate-900/50 backdrop-blur-sm">
        <Link href="/" className="flex items-center justify-center">
          <div className="flex items-center space-x-2">
            <div className="w-8 h-8 bg-gradient-to-br from-purple-400 to-pink-400 rounded-lg flex items-center justify-center">
              <Sword className="w-5 h-5 text-white" />
            </div>
            <span className="text-xl font-bold bg-gradient-to-r from-purple-400 to-pink-400 bg-clip-text text-transparent">
              Rivals
            </span>
          </div>
        </Link>
        <div className="flex-1 max-w-md mx-4">
          <div className="flex gap-2">
            <Input
              type="text"
              placeholder="다른 소환사 검색..."
              value={searchInput}
              onChange={(e) => {
                setSearchInput(e.target.value);
                setSearchError("");
              }}
              onKeyPress={(e) => e.key === "Enter" && handleNewSearch()}
              className="flex-1 bg-slate-800/50 border-slate-600 text-white placeholder:text-gray-400"
            />
            <Button
              onClick={handleNewSearch}
              size="sm"
              className="bg-purple-600 hover:bg-purple-700"
            >
              <Search className="w-4 h-4" />
            </Button>
          </div>
          {searchError && (
            <p className="text-xs text-red-400 mt-1">{searchError}</p>
          )}
        </div>
        <nav className="ml-auto flex gap-4 sm:gap-6 items-center">
          <Button
            onClick={openRivalryCart}
            variant="ghost"
            className="text-gray-300 hover:text-purple-400 relative"
          >
            <ShoppingCart className="w-4 h-4 mr-2" />
            라이벌리 카트
            {getTotalPlayerCount() > 0 && (
              <Badge className="absolute -top-2 -right-2 bg-purple-600 text-white text-xs min-w-[20px] h-5 flex items-center justify-center rounded-full">
                {getTotalPlayerCount()}
              </Badge>
            )}
          </Button>
          <Button
            onClick={() => router.back()}
            variant="ghost"
            className="text-gray-300 hover:text-purple-400"
          >
            <ArrowLeft className="w-4 h-4 mr-2" />
            뒤로가기
          </Button>
        </nav>
      </header>

      <main className="container mx-auto px-4 py-12">
        {account && tftStatus && (
          <>
            {/* 소환사 정보 헤더 */}
            <div className="text-center mb-12">
              <h1 className="text-4xl font-bold text-white mb-2">
                {account.gameName}#{account.tagLine}
              </h1>
              <p className="text-gray-400">TFT 전적 정보</p>
            </div>

            {/* 메인 스탯 카드들 */}
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3 mb-12">
              <Card className="bg-slate-800/50 border-purple-700/50 hover:border-purple-600 transition-colors">
                <CardHeader className="text-center">
                  <div className="w-16 h-16 bg-gradient-to-br from-purple-500 to-pink-500 rounded-full flex items-center justify-center mx-auto mb-4">
                    <Crown className="w-8 h-8 text-white" />
                  </div>
                  <CardTitle className="text-purple-400">현재 랭크</CardTitle>
                  <div className="text-3xl font-bold text-white">
                    {tftStatus.tier} {tftStatus.rank}
                  </div>
                  <CardDescription className="text-gray-400 text-lg">
                    {tftStatus.leaguePoints} LP
                  </CardDescription>
                </CardHeader>
              </Card>

              <Card className="bg-slate-800/50 border-green-700/50 hover:border-green-600 transition-colors">
                <CardHeader className="text-center">
                  <div className="w-16 h-16 bg-gradient-to-br from-green-500 to-emerald-500 rounded-full flex items-center justify-center mx-auto mb-4">
                    <Trophy className="w-8 h-8 text-white" />
                  </div>
                  <CardTitle className="text-green-400">승리</CardTitle>
                  <div className="text-3xl font-bold text-white">
                    {tftStatus.wins}
                  </div>
                  <CardDescription className="text-gray-400">
                    1등 횟수
                  </CardDescription>
                </CardHeader>
              </Card>

              <Card className="bg-slate-800/50 border-red-700/50 hover:border-red-600 transition-colors">
                <CardHeader className="text-center">
                  <div className="w-16 h-16 bg-gradient-to-br from-red-500 to-rose-500 rounded-full flex items-center justify-center mx-auto mb-4">
                    <Target className="w-8 h-8 text-white" />
                  </div>
                  <CardTitle className="text-red-400">패배</CardTitle>
                  <div className="text-3xl font-bold text-white">
                    {tftStatus.losses}
                  </div>
                  <CardDescription className="text-gray-400">
                    2-8등 횟수
                  </CardDescription>
                </CardHeader>
              </Card>
            </div>

            {/* 추가 정보 */}
            <div className="text-center mb-12">
              {tftStatus.hotStreak && (
                <Badge className="bg-gradient-to-r from-orange-500 to-red-500 text-white text-lg px-4 py-2">
                  <Star className="w-4 h-4 mr-2" />
                  연승 중!
                </Badge>
              )}
            </div>

            {/* 액션 버튼들 */}
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Button
                size="lg"
                onClick={handleAddToRivalry}
                className="bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-700 hover:to-pink-700"
              >
                <Users className="w-5 h-5 mr-2" />
                라이벌리에 추가하기
              </Button>
              <Button
                size="lg"
                variant="outline"
                className="border-purple-700 text-purple-300 hover:bg-purple-900/50"
                onClick={() => window.location.reload()}
              >
                전적 새로고침
              </Button>
            </div>
          </>
        )}
      </main>

      <TeamSelectionModal
        player={selectedPlayer}
        isOpen={isTeamModalOpen}
        onClose={() => {
          setIsTeamModalOpen(false);
          setSelectedPlayer(null);
        }}
      />
    </div>
  );
}
