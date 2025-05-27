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
