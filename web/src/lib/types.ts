// API Response wrapper type
export interface ApiResponse<T> {
  data: T;
  status: number;
  message?: string;
}

// Base DTO interfaces
export interface BaseResponseDto<T> {
  success: boolean;
  message?: string;
  data?: T;
}

// Riot Account related types
export interface RiotAccountDto {
  puuid: string;
  gameName: string;
  tagLine: string;
  id: string;
  updatedAt: string | null;
}

// TFT related types
export interface TftStatusDto {
  queueType: string; // 큐 타입 (RANKED_TFT, RANKED_TFT_TURBO, RANKED_TFT_DOUBLE_UP)
  tier: string;
  rank: string;
  leaguePoints: number;
  wins: number;
  losses: number;
  hotStreak: boolean;
  averageRank?: number;
  top4Rate?: number;
  lpChange?: number;
  winStreak?: number;
  loseStreak?: number;
  serverRank?: number;
  totalGames?: number;
  recentGames?: number[];
  seasonHigh?: { tier: string; lp: number };
  preferredComps?: string[];
}

// Rivalry related types
export interface RivalryCreationDto {
  participants: {
    // 정밀도 손실 문제로 인해 id를 string으로 설정
    id: string;
    side: "LEFT" | "RIGHT";
  }[];
}

export interface RivalryResultDto {
  rivalryId: string;
}

export interface RivalryDetailDto {
  rivalryId: string;
  leftStats: ParticipantStatDto[];
  rightStats: ParticipantStatDto[];
  createdAt: string;
}

export interface ParticipantStatDto {
  id: string;
  fullName: string;
  statistics: TftStatusDto;
}

// Summoner related types
export interface SummonerDto {
  id: string;
  accountId: string;
  puuid: string;
  name: string;
  profileIconId: number;
  revisionDate: number;
  summonerLevel: number;
}

export interface LeagueEntryDTO {
  leagueId: string;
  summonerId: string;
  summonerName: string;
  queueType: string;
  tier: string;
  rank: string;
  leaguePoints: number;
  wins: number;
  losses: number;
  hotStreak: boolean;
  veteran: boolean;
  freshBlood: boolean;
  inactive: boolean;
}

export interface TftRecentMatchDto {
  id: string;
  matchId: string;
  gameCreation: number;
  gameLength: number;
  level: number;
  placement: number;
  queueType: string;
  traits: TftMatchTrait[];
  units: TftMatchUnit[];
  participants: TftMatchParticipant[];
}

export interface TftMatchParticipant {
  puuid: string;
  level: number;
  placement: number;
  totalDamageToPlayers: number;
  riotIdGameName: string;
  riotIdTagline: string;
  traits: TftMatchTrait[];
  units: TftMatchUnit[];
}

export interface TftMatchTrait {
  name: string;
  num_units: number;
  style: number;
  tier_current: number;
  tier_total: number;
}

export interface TftMatchUnit {
  character_id: string;
  itemNames: string[];
  name: string;
  rarity: number;
  tier: number;
}

export interface TftBadgeDto {
  badgeType: string;
  achievementType: string;
  currentCount: number;
  requiredCount: number;
  isActive: boolean;
}

export interface TftRenewDto {
  matchesUpdated: number;
  entriesUpdated: boolean;
  badgesUpdated: boolean;
  lastUpdatedAt: string;
}
