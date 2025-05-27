// 목업 데이터
export const mockPlayers = [
  { puuid: "1", gameName: "Hide on bush", tagLine: "KR1" },
  { puuid: "2", gameName: "Faker", tagLine: "KR1" },
  { puuid: "3", gameName: "승상싱", tagLine: "KR1" },
  { puuid: "4", gameName: "DWG KIA ShowMaker", tagLine: "KR1" },
  { puuid: "5", gameName: "T1 Zeus", tagLine: "KR1" },
  { puuid: "6", gameName: "Canyon", tagLine: "KR1" },
  { puuid: "7", gameName: "Keria", tagLine: "KR1" },
  { puuid: "8", gameName: "Gumayusi", tagLine: "KR1" },
  { puuid: "9", gameName: "Oner", tagLine: "KR1" },
  { puuid: "10", gameName: "BeryL", tagLine: "KR1" },
];

// 확장된 TFT 데이터
export const mockTftData = {
  "Hide on bush#KR1": {
    account: { puuid: "1", gameName: "Hide on bush", tagLine: "KR1" },
    tftStatus: {
      tier: "CHALLENGER",
      rank: "I",
      leaguePoints: 1247,
      wins: 89,
      losses: 45,
      hotStreak: true,
      // 추가 데이터
      averageRank: 2.1,
      top4Rate: 78.5,
      lpChange: +45,
      winStreak: 7,
      serverRank: 23,
      totalGames: 134,
      recentGames: [1, 2, 1, 3, 1, 4, 2, 1],
      seasonHigh: { tier: "CHALLENGER", lp: 1389 },
      preferredComps: ["Reroll", "Fast 8", "Flex"],
    },
  },
  "Faker#KR1": {
    account: { puuid: "2", gameName: "Faker", tagLine: "KR1" },
    tftStatus: {
      tier: "GRANDMASTER",
      rank: "I",
      leaguePoints: 892,
      wins: 67,
      losses: 38,
      hotStreak: false,
      averageRank: 3.2,
      top4Rate: 65.7,
      lpChange: -18,
      winStreak: 0,
      loseStreak: 2,
      serverRank: 156,
      totalGames: 105,
      recentGames: [6, 5, 2, 7, 3, 1, 4, 8],
      seasonHigh: { tier: "CHALLENGER", lp: 1024 },
      preferredComps: ["Slow Roll", "Standard", "Econ"],
    },
  },
  "승상싱#KR1": {
    account: { puuid: "3", gameName: "승상싱", tagLine: "KR1" },
    tftStatus: {
      tier: "MASTER",
      rank: "I",
      leaguePoints: 456,
      wins: 45,
      losses: 32,
      hotStreak: true,
      averageRank: 2.8,
      top4Rate: 71.4,
      lpChange: +32,
      winStreak: 4,
      serverRank: 445,
      totalGames: 77,
      recentGames: [2, 1, 3, 1, 2, 5, 1, 3],
      seasonHigh: { tier: "MASTER", lp: 623 },
      preferredComps: ["Hyperroll", "Fast 8", "Flex"],
    },
  },
  "DWG KIA ShowMaker#KR1": {
    account: { puuid: "4", gameName: "DWG KIA ShowMaker", tagLine: "KR1" },
    tftStatus: {
      tier: "DIAMOND",
      rank: "I",
      leaguePoints: 78,
      wins: 34,
      losses: 28,
      hotStreak: false,
      averageRank: 4.1,
      top4Rate: 58.1,
      lpChange: +12,
      winStreak: 1,
      serverRank: 1247,
      totalGames: 62,
      recentGames: [3, 6, 4, 2, 7, 3, 1, 5],
      seasonHigh: { tier: "DIAMOND", lp: 234 },
      preferredComps: ["Standard", "Econ", "Slow Roll"],
    },
  },
  "T1 Zeus#KR1": {
    account: { puuid: "5", gameName: "T1 Zeus", tagLine: "KR1" },
    tftStatus: {
      tier: "DIAMOND",
      rank: "II",
      leaguePoints: 45,
      wins: 28,
      losses: 25,
      hotStreak: false,
      averageRank: 4.5,
      top4Rate: 52.8,
      lpChange: -8,
      winStreak: 0,
      loseStreak: 1,
      serverRank: 1856,
      totalGames: 53,
      recentGames: [5, 4, 6, 3, 8, 2, 7, 4],
      seasonHigh: { tier: "DIAMOND", lp: 156 },
      preferredComps: ["Standard", "Flex", "Econ"],
    },
  },
};

// 라이벌리 목업 데이터
export interface ParticipantStatDto {
  puuid: string;
  gameName: string;
  tagLine: string;
  tier: string;
  rank: string;
  leaguePoints: number;
  wins: number;
  losses: number;
  hotStreak: boolean;
}

export interface RivalryDetailDto {
  rivalryId: number;
  leftStats: ParticipantStatDto[];
  rightStats: ParticipantStatDto[];
  createdAt: string;
}

export const mockRivalries: Record<number, RivalryDetailDto> = {
  1: {
    rivalryId: 1,
    leftStats: [
      {
        puuid: "1",
        gameName: "Hide on bush",
        tagLine: "KR1",
        tier: "CHALLENGER",
        rank: "I",
        leaguePoints: 1247,
        wins: 89,
        losses: 45,
        hotStreak: true,
      },
    ],
    rightStats: [
      {
        puuid: "2",
        gameName: "Faker",
        tagLine: "KR1",
        tier: "GRANDMASTER",
        rank: "I",
        leaguePoints: 892,
        wins: 67,
        losses: 38,
        hotStreak: false,
      },
      {
        puuid: "4",
        gameName: "DWG KIA ShowMaker",
        tagLine: "KR1",
        tier: "DIAMOND",
        rank: "I",
        leaguePoints: 78,
        wins: 34,
        losses: 28,
        hotStreak: false,
      },
      {
        puuid: "3",
        gameName: "승상싱",
        tagLine: "KR1",
        tier: "MASTER",
        rank: "I",
        leaguePoints: 456,
        wins: 45,
        losses: 32,
        hotStreak: true,
      },
    ],
    createdAt: "2024-01-15T10:30:00",
  },
  2: {
    rivalryId: 2,
    leftStats: [
      {
        puuid: "5",
        gameName: "T1 Zeus",
        tagLine: "KR1",
        tier: "DIAMOND",
        rank: "II",
        leaguePoints: 45,
        wins: 28,
        losses: 25,
        hotStreak: false,
      },
    ],
    rightStats: [
      {
        puuid: "6",
        gameName: "Canyon",
        tagLine: "KR1",
        tier: "DIAMOND",
        rank: "I",
        leaguePoints: 234,
        wins: 42,
        losses: 31,
        hotStreak: true,
      },
    ],
    createdAt: "2024-01-14T15:45:00",
  },
};

// 라이벌리 생성 시뮬레이션
let nextRivalryId = 3;

export function createMockRivalry(leftTeam: any[], rightTeam: any[]): number {
  const rivalryId = nextRivalryId++;

  const leftStats: ParticipantStatDto[] = leftTeam.map((player) => {
    const tftData = Object.values(mockTftData).find(
      (data) => data.account.puuid === player.puuid
    );
    return {
      puuid: player.puuid,
      gameName: player.gameName,
      tagLine: player.tagLine,
      tier: tftData?.tftStatus.tier || "UNRANKED",
      rank: tftData?.tftStatus.rank || "I",
      leaguePoints: tftData?.tftStatus.leaguePoints || 0,
      wins: tftData?.tftStatus.wins || 0,
      losses: tftData?.tftStatus.losses || 0,
      hotStreak: tftData?.tftStatus.hotStreak || false,
    };
  });

  const rightStats: ParticipantStatDto[] = rightTeam.map((player) => {
    const tftData = Object.values(mockTftData).find(
      (data) => data.account.puuid === player.puuid
    );
    return {
      puuid: player.puuid,
      gameName: player.gameName,
      tagLine: player.tagLine,
      tier: tftData?.tftStatus.tier || "UNRANKED",
      rank: tftData?.tftStatus.rank || "I",
      leaguePoints: tftData?.tftStatus.leaguePoints || 0,
      wins: tftData?.tftStatus.wins || 0,
      losses: tftData?.tftStatus.losses || 0,
      hotStreak: tftData?.tftStatus.hotStreak || false,
    };
  });

  mockRivalries[rivalryId] = {
    rivalryId,
    leftStats,
    rightStats,
    createdAt: new Date().toISOString(),
  };

  return rivalryId;
}

// 티어별 색상 및 아이콘 정보
export const tierInfo = {
  CHALLENGER: {
    color: "from-yellow-400 to-orange-500",
    bgColor: "bg-yellow-500/10",
    borderColor: "border-yellow-500/30",
    textColor: "text-yellow-400",
    icon: "👑",
  },
  GRANDMASTER: {
    color: "from-red-400 to-pink-500",
    bgColor: "bg-red-500/10",
    borderColor: "border-red-500/30",
    textColor: "text-red-400",
    icon: "💎",
  },
  MASTER: {
    color: "from-purple-400 to-indigo-500",
    bgColor: "bg-purple-500/10",
    borderColor: "border-purple-500/30",
    textColor: "text-purple-400",
    icon: "🔮",
  },
  DIAMOND: {
    color: "from-blue-400 to-cyan-500",
    bgColor: "bg-blue-500/10",
    borderColor: "border-blue-500/30",
    textColor: "text-blue-400",
    icon: "💠",
  },
  PLATINUM: {
    color: "from-teal-400 to-green-500",
    bgColor: "bg-teal-500/10",
    borderColor: "border-teal-500/30",
    textColor: "text-teal-400",
    icon: "🌟",
  },
  GOLD: {
    color: "from-yellow-500 to-amber-500",
    bgColor: "bg-yellow-500/10",
    borderColor: "border-yellow-500/30",
    textColor: "text-yellow-500",
    icon: "⭐",
  },
  SILVER: {
    color: "from-gray-400 to-slate-500",
    bgColor: "bg-gray-500/10",
    borderColor: "border-gray-500/30",
    textColor: "text-gray-400",
    icon: "🥈",
  },
  BRONZE: {
    color: "from-orange-600 to-amber-700",
    bgColor: "bg-orange-600/10",
    borderColor: "border-orange-600/30",
    textColor: "text-orange-600",
    icon: "🥉",
  },
  IRON: {
    color: "from-gray-600 to-gray-700",
    bgColor: "bg-gray-600/10",
    borderColor: "border-gray-600/30",
    textColor: "text-gray-500",
    icon: "⚫",
  },
  UNRANKED: {
    color: "from-gray-500 to-gray-600",
    bgColor: "bg-gray-500/10",
    borderColor: "border-gray-500/30",
    textColor: "text-gray-500",
    icon: "❓",
  },
};
