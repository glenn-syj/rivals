"use client";

import { useState, useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { ArrowLeft, Target, Star, Sword, Calendar, Users } from "lucide-react";
import Link from "next/link";
import {
  mockRivalries,
  type RivalryDetailDto,
  type ParticipantStatDto,
} from "@/lib/mockData";

function PlayerStatCard({
  player,
  teamColor,
}: {
  player: ParticipantStatDto;
  teamColor: "blue" | "red";
}) {
  const winRate =
    player.wins + player.losses > 0
      ? (player.wins / (player.wins + player.losses)) * 100
      : 0;
  const borderColor =
    teamColor === "blue" ? "border-blue-700/50" : "border-red-700/50";
  const hoverColor =
    teamColor === "blue" ? "hover:border-blue-600" : "hover:border-red-600";

  return (
    <Card
      className={`bg-slate-800/50 ${borderColor} ${hoverColor} transition-colors`}
    >
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between">
          <div>
            <CardTitle className="text-white text-lg">
              {player.gameName}#{player.tagLine}
            </CardTitle>
            <CardDescription className="text-gray-400">
              {player.tier} {player.rank} • {player.leaguePoints} LP
            </CardDescription>
          </div>
          {player.hotStreak && (
            <Badge className="bg-gradient-to-r from-orange-500 to-red-500 text-white">
              <Star className="w-3 h-3 mr-1" />
              연승
            </Badge>
          )}
        </div>
      </CardHeader>
      <CardContent className="space-y-3">
        <div className="grid grid-cols-3 gap-4 text-center">
          <div>
            <p className="text-2xl font-bold text-green-400">{player.wins}</p>
            <p className="text-xs text-gray-400">승리</p>
          </div>
          <div>
            <p className="text-2xl font-bold text-red-400">{player.losses}</p>
            <p className="text-xs text-gray-400">패배</p>
          </div>
          <div>
            <p className="text-2xl font-bold text-purple-400">
              {winRate.toFixed(1)}%
            </p>
            <p className="text-xs text-gray-400">승률</p>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}

function TeamStats({
  players,
  teamName,
  teamColor,
}: {
  players: ParticipantStatDto[];
  teamName: string;
  teamColor: "blue" | "red";
}) {
  const totalWins = players.reduce((sum, p) => sum + p.wins, 0);
  const totalLosses = players.reduce((sum, p) => sum + p.losses, 0);
  const avgLP =
    players.length > 0
      ? players.reduce((sum, p) => sum + p.leaguePoints, 0) / players.length
      : 0;
  const teamWinRate =
    totalWins + totalLosses > 0
      ? (totalWins / (totalWins + totalLosses)) * 100
      : 0;

  const titleColor = teamColor === "blue" ? "text-blue-400" : "text-red-400";
  const bgColor = teamColor === "blue" ? "bg-blue-900/20" : "bg-red-900/20";
  const borderColor =
    teamColor === "blue" ? "border-blue-700/50" : "border-red-700/50";

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className={`text-2xl font-bold ${titleColor} flex items-center`}>
          <div
            className={`w-4 h-4 ${
              teamColor === "blue" ? "bg-blue-500" : "bg-red-500"
            } rounded-full mr-3`}
          />
          {teamName}
        </h2>
        <Badge variant="secondary" className="bg-purple-900/50 text-purple-300">
          {players.length}명
        </Badge>
      </div>

      {/* 팀 통계 요약 */}
      <Card className={`${bgColor} ${borderColor}`}>
        <CardContent className="p-4">
          <div className="grid grid-cols-3 gap-4 text-center">
            <div>
              <p className="text-xl font-bold text-white">{totalWins}</p>
              <p className="text-xs text-gray-400">총 승리</p>
            </div>
            <div>
              <p className="text-xl font-bold text-white">{avgLP.toFixed(0)}</p>
              <p className="text-xs text-gray-400">평균 LP</p>
            </div>
            <div>
              <p className="text-xl font-bold text-white">
                {teamWinRate.toFixed(1)}%
              </p>
              <p className="text-xs text-gray-400">팀 승률</p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* 플레이어 카드들 */}
      <div className="space-y-3">
        {players.length === 0 ? (
          <div className="text-center py-8 text-gray-400">
            <Users className="w-8 h-8 mx-auto mb-2 opacity-50" />
            <p>팀에 플레이어가 없습니다</p>
          </div>
        ) : (
          players.map((player) => (
            <PlayerStatCard
              key={player.puuid}
              player={player}
              teamColor={teamColor}
            />
          ))
        )}
      </div>
    </div>
  );
}

export default function RivalryDetailPage() {
  const params = useParams();
  const router = useRouter();
  const rivalryId = Number.parseInt(params.rivalryId as string);

  const [rivalry, setRivalry] = useState<RivalryDetailDto | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchRivalry = async () => {
      // 목업 데이터에서 조회
      const rivalryData = mockRivalries[rivalryId];

      if (rivalryData) {
        setRivalry(rivalryData);
      } else {
        setError("라이벌리를 찾을 수 없습니다");
      }

      setIsLoading(false);
    };

    fetchRivalry();
  }, [rivalryId]);

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900 flex items-center justify-center">
        <div className="text-center">
          <div className="w-16 h-16 border-4 border-purple-500 border-t-transparent rounded-full animate-spin mx-auto mb-4" />
          <p className="text-white text-lg">라이벌리를 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (error || !rivalry) {
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

  const createdDate = new Date(rivalry.createdAt).toLocaleDateString("ko-KR", {
    year: "numeric",
    month: "long",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });

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
        <nav className="ml-auto flex gap-4 sm:gap-6 items-center">
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
        {/* 라이벌리 헤더 */}
        <div className="text-center mb-12">
          <div className="flex items-center justify-center mb-4">
            <Badge
              variant="secondary"
              className="bg-purple-900/50 text-purple-300 border-purple-700"
            >
              <Calendar className="w-3 h-3 mr-1" />
              {createdDate}
            </Badge>
          </div>
          <h1 className="text-4xl font-bold text-white mb-2">
            라이벌리 #{rivalry.rivalryId}
          </h1>
          <p className="text-gray-400">
            {rivalry.leftStats.length + rivalry.rightStats.length}명의
            플레이어가 참여하는 라이벌 관계
          </p>
        </div>

        {/* 팀 비교 - 반응형 레이아웃 */}
        <div className="space-y-8 lg:space-y-0 lg:grid lg:grid-cols-2 lg:gap-8 mb-12">
          <TeamStats
            players={rivalry.leftStats}
            teamName="LEFT TEAM"
            teamColor="blue"
          />
          <TeamStats
            players={rivalry.rightStats}
            teamName="RIGHT TEAM"
            teamColor="red"
          />
        </div>

        {/* 액션 버튼들 */}
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <Button
            size="lg"
            variant="outline"
            className="border-purple-700 text-purple-300 hover:bg-purple-900/50"
            onClick={() => router.push("/")}
          >
            <Users className="w-5 h-5 mr-2" />새 라이벌리 만들기
          </Button>
        </div>
      </main>
    </div>
  );
}
