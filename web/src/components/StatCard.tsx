"use client";

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import {
  TrendingUp,
  TrendingDown,
  Minus,
  Trophy,
  BarChart3,
  Flame,
  Zap,
} from "lucide-react";
import { tierInfo } from "@/lib/mockData";

interface StatCardProps {
  type: "rank" | "performance" | "streak" | "recent";
  data: any;
  className?: string;
}

function CircularProgress({
  value,
  size = 60,
  strokeWidth = 6,
  color = "text-indigo-400",
}: {
  value: number;
  size?: number;
  strokeWidth?: number;
  color?: string;
}) {
  const radius = (size - strokeWidth) / 2;
  const circumference = radius * 2 * Math.PI;
  const strokeDasharray = `${(value / 100) * circumference} ${circumference}`;

  return (
    <div className="relative" style={{ width: size, height: size }}>
      <svg width={size} height={size} className="transform -rotate-90">
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          stroke="currentColor"
          strokeWidth={strokeWidth}
          fill="none"
          className="text-slate-700"
        />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          stroke="currentColor"
          strokeWidth={strokeWidth}
          fill="none"
          strokeDasharray={strokeDasharray}
          className={color}
          strokeLinecap="round"
        />
      </svg>
      <div className="absolute inset-0 flex items-center justify-center">
        <span className="text-sm font-bold text-white">
          {value.toFixed(0)}%
        </span>
      </div>
    </div>
  );
}

function RecentGamesChart({ games }: { games: number[] }) {
  const maxRank = 8;

  return (
    <div className="flex items-end gap-1 h-12">
      {games.map((rank, index) => {
        const height = ((maxRank - rank + 1) / maxRank) * 100;
        const isGood = rank <= 4;

        return (
          <div
            key={index}
            className={`w-3 rounded-t transition-all ${
              isGood
                ? "bg-gradient-to-t from-green-600 to-green-400"
                : "bg-gradient-to-t from-red-600 to-red-400"
            }`}
            style={{ height: `${height}%` }}
            title={`${rank}등`}
          />
        );
      })}
    </div>
  );
}

export default function StatCard({
  type,
  data,
  className = "",
}: StatCardProps) {
  if (type === "rank") {
    const tier =
      tierInfo[data.tier as keyof typeof tierInfo] || tierInfo.UNRANKED;

    return (
      <Card
        className={`bg-slate-800/50 border-slate-700/50 hover:border-indigo-500/50 transition-all duration-300 ${className}`}
      >
        <CardHeader className="pb-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div
                className={`w-12 h-12 bg-gradient-to-br ${tier.color} rounded-xl flex items-center justify-center text-2xl`}
              >
                {tier.icon}
              </div>
              <div>
                <CardTitle className={`${tier.textColor} text-lg`}>
                  현재 랭크
                </CardTitle>
                <CardDescription className="text-gray-400">
                  서버 순위 #{data.serverRank}
                </CardDescription>
              </div>
            </div>
            {data.lpChange !== 0 && (
              <Badge
                className={`${
                  data.lpChange > 0 ? "bg-green-600" : "bg-red-600"
                } text-white`}
              >
                {data.lpChange > 0 ? "+" : ""}
                {data.lpChange} LP
                {data.lpChange > 0 ? (
                  <TrendingUp className="w-3 h-3 ml-1" />
                ) : (
                  <TrendingDown className="w-3 h-3 ml-1" />
                )}
              </Badge>
            )}
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="text-center">
            <div className="text-3xl font-bold text-white mb-1">
              {data.tier} {data.rank}
            </div>
            <div className="text-xl text-gray-300">{data.leaguePoints} LP</div>
          </div>

          <div className="grid grid-cols-2 gap-4 text-center">
            <div
              className={`p-3 rounded-lg ${tier.bgColor} ${tier.borderColor} border`}
            >
              <div className="text-lg font-bold text-white">
                {data.totalGames}
              </div>
              <div className="text-xs text-gray-400">총 게임</div>
            </div>
            <div
              className={`p-3 rounded-lg ${tier.bgColor} ${tier.borderColor} border`}
            >
              <div className="text-lg font-bold text-white">
                {data.averageRank.toFixed(1)}
              </div>
              <div className="text-xs text-gray-400">평균 순위</div>
            </div>
          </div>
        </CardContent>
      </Card>
    );
  }

  if (type === "performance") {
    const winRate =
      data.wins + data.losses > 0
        ? (data.wins / (data.wins + data.losses)) * 100
        : 0;

    return (
      <Card
        className={`bg-slate-800/50 border-slate-700/50 hover:border-green-500/50 transition-all duration-300 ${className}`}
      >
        <CardHeader className="pb-3">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 bg-gradient-to-br from-green-500 to-emerald-500 rounded-xl flex items-center justify-center">
              <Trophy className="w-6 h-6 text-white" />
            </div>
            <div>
              <CardTitle className="text-green-400 text-lg">
                게임 성과
              </CardTitle>
              <CardDescription className="text-gray-400">
                승률 & 탑4 비율
              </CardDescription>
            </div>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <div className="text-center">
              <CircularProgress value={winRate} color="text-green-400" />
              <div className="text-xs text-gray-400 mt-1">승률</div>
            </div>
            <div className="text-center">
              <CircularProgress value={data.top4Rate} color="text-blue-400" />
              <div className="text-xs text-gray-400 mt-1">탑4 비율</div>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4 text-center">
            <div className="p-3 rounded-lg bg-green-500/10 border border-green-500/30">
              <div className="text-2xl font-bold text-green-400">
                {data.wins}
              </div>
              <div className="text-xs text-gray-400">승리 (1등)</div>
            </div>
            <div className="p-3 rounded-lg bg-red-500/10 border border-red-500/30">
              <div className="text-2xl font-bold text-red-400">
                {data.losses}
              </div>
              <div className="text-xs text-gray-400">패배 (2-8등)</div>
            </div>
          </div>
        </CardContent>
      </Card>
    );
  }

  if (type === "streak") {
    const isWinStreak = data.winStreak > 0;
    const isLoseStreak = data.loseStreak > 0;
    const streakValue = isWinStreak
      ? data.winStreak
      : isLoseStreak
      ? data.loseStreak
      : 0;

    return (
      <Card
        className={`bg-slate-800/50 border-slate-700/50 hover:border-orange-500/50 transition-all duration-300 ${className}`}
      >
        <CardHeader className="pb-3">
          <div className="flex items-center gap-3">
            <div
              className={`w-12 h-12 bg-gradient-to-br ${
                isWinStreak
                  ? "from-orange-500 to-red-500"
                  : isLoseStreak
                  ? "from-gray-600 to-gray-700"
                  : "from-slate-600 to-slate-700"
              } rounded-xl flex items-center justify-center`}
            >
              {isWinStreak ? (
                <Flame className="w-6 h-6 text-white" />
              ) : isLoseStreak ? (
                <TrendingDown className="w-6 h-6 text-white" />
              ) : (
                <Minus className="w-6 h-6 text-white" />
              )}
            </div>
            <div>
              <CardTitle
                className={`${
                  isWinStreak
                    ? "text-orange-400"
                    : isLoseStreak
                    ? "text-gray-400"
                    : "text-slate-400"
                } text-lg`}
              >
                현재 스트릭
              </CardTitle>
              <CardDescription className="text-gray-400">
                {data.hotStreak ? "핫스트릭 중!" : "최근 경기 흐름"}
              </CardDescription>
            </div>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="text-center">
            {streakValue > 0 ? (
              <>
                <div
                  className={`text-4xl font-bold ${
                    isWinStreak ? "text-orange-400" : "text-gray-400"
                  } mb-2`}
                >
                  {streakValue}
                </div>
                <div
                  className={`text-lg ${
                    isWinStreak ? "text-orange-300" : "text-gray-300"
                  }`}
                >
                  {isWinStreak ? "연승" : "연패"}
                </div>
                {data.hotStreak && (
                  <Badge className="bg-gradient-to-r from-orange-500 to-red-500 text-white mt-2">
                    <Zap className="w-3 h-3 mr-1" />
                    핫스트릭!
                  </Badge>
                )}
              </>
            ) : (
              <>
                <div className="text-3xl font-bold text-slate-400 mb-2">-</div>
                <div className="text-lg text-slate-400">스트릭 없음</div>
              </>
            )}
          </div>

          <div className="space-y-2">
            <div className="flex justify-between text-sm">
              <span className="text-gray-400">시즌 최고</span>
              <span className="text-white">
                {data.seasonHigh.tier} {data.seasonHigh.lp} LP
              </span>
            </div>
            <Progress
              value={(data.leaguePoints / data.seasonHigh.lp) * 100}
              className="h-2"
            />
          </div>
        </CardContent>
      </Card>
    );
  }

  if (type === "recent") {
    return (
      <Card
        className={`bg-slate-800/50 border-slate-700/50 hover:border-purple-500/50 transition-all duration-300 ${className}`}
      >
        <CardHeader className="pb-3">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 bg-gradient-to-br from-purple-500 to-indigo-500 rounded-xl flex items-center justify-center">
              <BarChart3 className="w-6 h-6 text-white" />
            </div>
            <div>
              <CardTitle className="text-purple-400 text-lg">
                최근 게임
              </CardTitle>
              <CardDescription className="text-gray-400">
                최근 8게임 순위
              </CardDescription>
            </div>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex justify-center">
            <RecentGamesChart games={data.recentGames} />
          </div>

          <div className="grid grid-cols-3 gap-3 text-center">
            <div className="p-2 rounded-lg bg-purple-500/10 border border-purple-500/30">
              <div className="text-lg font-bold text-purple-400">
                {data.recentGames.filter((rank: number) => rank === 1).length}
              </div>
              <div className="text-xs text-gray-400">1등</div>
            </div>
            <div className="p-2 rounded-lg bg-blue-500/10 border border-blue-500/30">
              <div className="text-lg font-bold text-blue-400">
                {data.recentGames.filter((rank: number) => rank <= 4).length}
              </div>
              <div className="text-xs text-gray-400">탑4</div>
            </div>
            <div className="p-2 rounded-lg bg-red-500/10 border border-red-500/30">
              <div className="text-lg font-bold text-red-400">
                {data.recentGames.filter((rank: number) => rank >= 5).length}
              </div>
              <div className="text-xs text-gray-400">바텀4</div>
            </div>
          </div>

          <div className="space-y-1">
            <div className="text-xs text-gray-400 text-center">
              선호 플레이 스타일
            </div>
            <div className="flex gap-1 justify-center">
              {data.preferredComps.map((comp: string, index: number) => (
                <Badge
                  key={index}
                  variant="secondary"
                  className="bg-slate-700 text-slate-300 text-xs"
                >
                  {comp}
                </Badge>
              ))}
            </div>
          </div>
        </CardContent>
      </Card>
    );
  }

  return null;
}
