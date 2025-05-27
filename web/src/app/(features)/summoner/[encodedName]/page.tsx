"use client";

import { useState, useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardDescription,
  CardHeader,
  CardTitle,
  CardContent,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import {
  ArrowLeft,
  Users,
  Sword,
  ShoppingCart,
  Search,
  Crown,
  Star,
  BarChart3,
  Clock,
  Trophy,
} from "lucide-react";
import Link from "next/link";
import { useRivalry } from "@/contexts/RivalryContext";
import { mockTftData } from "@/lib/mockData";
import { Input } from "@/components/ui/input";
import TeamSelectionModal from "@/components/TeamSelectionModal";
import type { Player } from "@/contexts/RivalryContext";

interface TftStatus {
  tier: string;
  rank: string;
  leaguePoints: number;
  wins: number;
  losses: number;
  hotStreak: boolean;
  averageRank: number;
  top4Rate: number;
  lpChange: number;
  winStreak?: number;
  loseStreak?: number;
  serverRank: number;
  totalGames: number;
  recentGames: number[];
  seasonHigh: { tier: string; lp: number };
  preferredComps: string[];
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
      <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 flex items-center justify-center">
        <div className="text-center">
          <div className="w-16 h-16 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin mx-auto mb-4" />
          <p className="text-white text-lg">전적을 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 flex items-center justify-center">
        <div className="text-center max-w-md">
          <Card className="bg-red-900/20 border-red-700/50">
            <CardHeader>
              <CardTitle className="text-red-400 flex items-center justify-center">
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
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
      {/* Header */}
      <header className="px-4 lg:px-6 h-16 flex items-center border-b border-slate-700/50 bg-slate-900/80 backdrop-blur-sm">
        <Link href="/" className="flex items-center justify-center">
          <div className="flex items-center space-x-2">
            <div className="w-8 h-8 bg-gradient-to-br from-slate-600 to-indigo-600 rounded-lg flex items-center justify-center">
              <Sword className="w-5 h-5 text-white" />
            </div>
            <span className="text-xl font-bold bg-gradient-to-r from-slate-400 to-indigo-400 bg-clip-text text-transparent">
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
              className="bg-indigo-600 hover:bg-indigo-700"
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
            className="text-gray-300 hover:text-indigo-400 relative"
          >
            <ShoppingCart className="w-4 h-4 mr-2" />
            라이벌리 카트
            {getTotalPlayerCount() > 0 && (
              <Badge className="absolute -top-2 -right-2 bg-indigo-600 text-white text-xs min-w-[20px] h-5 flex items-center justify-center rounded-full">
                {getTotalPlayerCount()}
              </Badge>
            )}
          </Button>
          <Button
            onClick={() => router.back()}
            variant="ghost"
            className="text-gray-300 hover:text-indigo-400"
          >
            <ArrowLeft className="w-4 h-4 mr-2" />
            뒤로가기
          </Button>
        </nav>
      </header>

      <main className="container mx-auto px-4 py-8">
        {account && tftStatus && (
          <>
            {/* 소환사 정보 헤더 */}
            <div className="text-center mb-8">
              <h1 className="text-4xl font-bold text-white mb-2">
                {account.gameName}#{account.tagLine}
              </h1>
              <p className="text-gray-400">TFT 전적 정보</p>
            </div>

            {/* 메인 레이아웃: 왼쪽 고정, 오른쪽 스크롤 */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 min-h-[calc(100vh-300px)]">
              {/* 왼쪽: TftStatus + Match 통계 (고정) */}
              <div className="lg:col-span-1 space-y-6 lg:sticky lg:top-8 lg:h-fit">
                {/* 1. TftStatus 카드 */}
                <Card className="bg-slate-800/50 border-slate-700/50 hover:border-indigo-500/50 transition-colors">
                  <CardHeader>
                    <div className="flex items-center justify-between">
                      <div>
                        <CardTitle className="text-xl text-white flex items-center gap-3">
                          <div className="w-10 h-10 bg-gradient-to-br from-slate-600 to-indigo-600 rounded-lg flex items-center justify-center">
                            <Crown className="w-5 h-5 text-white" />
                          </div>
                          {tftStatus.tier} {tftStatus.rank}
                        </CardTitle>
                        <CardDescription className="text-gray-300 mt-1">
                          {tftStatus.leaguePoints} LP • 서버 #
                          {tftStatus.serverRank || "N/A"}
                        </CardDescription>
                      </div>
                      {tftStatus.hotStreak && (
                        <Badge className="bg-gradient-to-r from-orange-500 to-red-500 text-white">
                          <Star className="w-3 h-3 mr-1" />
                          연승
                        </Badge>
                      )}
                    </div>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    {/* 기본 전적 */}
                    <div className="grid grid-cols-2 gap-3">
                      <div className="text-center p-3 rounded-lg bg-green-500/10 border border-green-500/30">
                        <div className="text-xl font-bold text-green-400">
                          {tftStatus.wins}
                        </div>
                        <div className="text-xs text-gray-400">승리</div>
                      </div>
                      <div className="text-center p-3 rounded-lg bg-red-500/10 border border-red-500/30">
                        <div className="text-xl font-bold text-red-400">
                          {tftStatus.losses}
                        </div>
                        <div className="text-xs text-gray-400">패배</div>
                      </div>
                    </div>

                    {/* 추가 정보 */}
                    <div className="space-y-2 pt-2 border-t border-slate-700">
                      <div className="flex justify-between">
                        <span className="text-sm text-gray-400">승률</span>
                        <span className="text-sm text-white">
                          {tftStatus.wins + tftStatus.losses > 0
                            ? (
                                (tftStatus.wins /
                                  (tftStatus.wins + tftStatus.losses)) *
                                100
                              ).toFixed(1)
                            : "0.0"}
                          %
                        </span>
                      </div>
                      {tftStatus.averageRank && (
                        <div className="flex justify-between">
                          <span className="text-sm text-gray-400">
                            평균 순위
                          </span>
                          <span className="text-sm text-white">
                            {tftStatus.averageRank.toFixed(1)}등
                          </span>
                        </div>
                      )}
                      {tftStatus.top4Rate && (
                        <div className="flex justify-between">
                          <span className="text-sm text-gray-400">
                            탑4 비율
                          </span>
                          <span className="text-sm text-white">
                            {tftStatus.top4Rate.toFixed(1)}%
                          </span>
                        </div>
                      )}
                    </div>
                  </CardContent>
                </Card>

                {/* 2. Match 통계 카드 */}
                <Card className="bg-slate-800/50 border-slate-700/50 hover:border-purple-500/50 transition-colors">
                  <CardHeader>
                    <CardTitle className="text-lg text-white flex items-center gap-3">
                      <div className="w-10 h-10 bg-gradient-to-br from-purple-500 to-indigo-500 rounded-lg flex items-center justify-center">
                        <BarChart3 className="w-5 h-5 text-white" />
                      </div>
                      Match 통계
                    </CardTitle>
                    <CardDescription className="text-gray-400">
                      게임별 상세 분석
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="text-center py-8 text-gray-400">
                      <BarChart3 className="w-8 h-8 mx-auto mb-3 opacity-50" />
                      <p className="text-sm">Match 데이터 수집 중...</p>
                      <p className="text-xs mt-1">
                        게임 기록 분석이 여기에 표시됩니다
                      </p>
                    </div>
                  </CardContent>
                </Card>

                {/* 액션 버튼 */}
                <div className="space-y-3">
                  <Button
                    onClick={handleAddToRivalry}
                    className="w-full bg-gradient-to-r from-slate-600 to-indigo-600 hover:from-slate-700 hover:to-indigo-700"
                  >
                    <Users className="w-4 h-4 mr-2" />
                    라이벌리에 추가하기
                  </Button>
                  <Button
                    variant="outline"
                    className="w-full border-slate-600 text-slate-300 hover:bg-slate-800/50"
                    onClick={() => window.location.reload()}
                  >
                    전적 새로고침
                  </Button>
                </div>
              </div>

              {/* 오른쪽: TFT 랭크 전적 (스크롤 가능) */}
              <div className="lg:col-span-2 space-y-6">
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
                    {/* 내부 전적 구분 */}
                    <div className="space-y-4">
                      {/* 최근 게임 섹션 */}
                      <div className="p-4 rounded-lg bg-slate-700/30 border border-slate-600/50">
                        <h3 className="text-lg font-semibold text-white mb-3 flex items-center gap-2">
                          <Clock className="w-4 h-4" />
                          최근 게임
                        </h3>
                        <div className="text-center py-8 text-gray-400">
                          <p>게임 기록이 여기에 표시됩니다</p>
                          <p className="text-sm mt-2">
                            (API 연동 후 구현 예정)
                          </p>
                        </div>
                      </div>

                      {/* 상세 분석 섹션 */}
                      <div className="p-4 rounded-lg bg-slate-700/30 border border-slate-600/50">
                        <h3 className="text-lg font-semibold text-white mb-3 flex items-center gap-2">
                          <BarChart3 className="w-4 h-4" />
                          상세 분석
                        </h3>
                        <div className="text-center py-8 text-gray-400">
                          <p>덱 조합, 아이템 빌드 분석이 여기에 표시됩니다</p>
                          <p className="text-sm mt-2">
                            (API 연동 후 구현 예정)
                          </p>
                        </div>
                      </div>

                      {/* 추가 섹션들... */}
                      <div className="p-4 rounded-lg bg-slate-700/30 border border-slate-600/50">
                        <h3 className="text-lg font-semibold text-white mb-3">
                          기타 통계
                        </h3>
                        <div className="text-center py-8 text-gray-400">
                          <p>추가 통계 정보가 여기에 표시됩니다</p>
                          <p className="text-sm mt-2">(확장 예정)</p>
                        </div>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </div>
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
