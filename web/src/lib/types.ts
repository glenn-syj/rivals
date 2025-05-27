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
export interface RiotAccountResponse {
  puuid: string;
  gameName: string;
  tagLine: string;
  id: number;
}

// TFT related types
export interface TftStatusDto {
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
    id: number;
    side: "LEFT" | "RIGHT";
  }[];
}

export interface RivalryResultDto {
  rivalryId: number;
}

export interface RivalryDetailDto {
  rivalryId: number;
  leftStats: ParticipantStatDto[];
  rightStats: ParticipantStatDto[];
  createdAt: string;
}

export interface ParticipantStatDto {
  id: number;
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
