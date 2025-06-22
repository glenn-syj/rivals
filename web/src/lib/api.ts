import axios from "axios";
import type {
  RiotAccountDto,
  TftStatusDto,
  RivalryCreationDto,
  RivalryResultDto,
  RivalryDetailDto,
  TftRecentMatchDto,
  TftBadgeDto,
  TftRenewDto,
} from "./types";

const API_URL = process.env.NEXT_PUBLIC_API_BASE_URL || "http://127.0.0.1:8080";

const api = axios.create({
  baseURL: API_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

export const findRiotAccount = async (
  gameName: string,
  tagLine: string
): Promise<RiotAccountDto> => {
  const trimmedGameName = gameName.trim();
  const trimmedTagLine = tagLine.trim();

  const response = await api.get<RiotAccountDto>(
    `/api/v1/riot/accounts/${trimmedGameName}/${trimmedTagLine}`
  );
  return response.data;
};

export const renewRiotAccount = async (
  gameName: string,
  tagLine: string
): Promise<RiotAccountDto> => {
  const trimmedGameName = gameName.trim();
  const trimmedTagLine = tagLine.trim();

  const response = await api.patch<RiotAccountDto>(
    `/api/v1/riot/accounts/renew/${trimmedGameName}/${trimmedTagLine}`
  );
  return response.data;
};

// TFT League Entry APIs
export const getTftStatus = async (
  gameName: string,
  tagLine: string
): Promise<TftStatusDto[]> => {
  const trimmedGameName = gameName.trim();
  const trimmedTagLine = tagLine.trim();

  const response = await api.get<TftStatusDto[]>(
    `/api/v1/tft/entries/${trimmedGameName}/${trimmedTagLine}`
  );

  return response.data;
};

// Rivalry APIs
export const createRivalry = async (
  creationDto: RivalryCreationDto
): Promise<RivalryResultDto> => {
  const response = await api.post<RivalryResultDto>(
    `/api/v1/rivalries`,
    JSON.stringify(creationDto)
  );

  return response.data;
};

export const getRivalryById = async (
  rivalryId: string
): Promise<RivalryDetailDto> => {
  const response = await api.get<RivalryDetailDto>(
    `/api/v1/rivalries/${rivalryId}`
  );
  return response.data;
};

// TFT Matches APIs
export const getTftMatches = async (
  gameName: string,
  tagLine: string
): Promise<TftRecentMatchDto[]> => {
  const trimmedGameName = gameName.trim();
  const trimmedTagLine = tagLine.trim();

  const response = await api.get<TftRecentMatchDto[]>(
    `/api/v1/tft/matches/${trimmedGameName}/${trimmedTagLine}`
  );
  return response.data;
};

// TFT Badge APIs
export const getTftBadges = async (
  gameName: string,
  tagLine: string
): Promise<TftBadgeDto[]> => {
  const trimmedGameName = gameName.trim();
  const trimmedTagLine = tagLine.trim();

  const response = await api.get<TftBadgeDto[]>(
    `/api/v1/tft/badges/${trimmedGameName}/${trimmedTagLine}`
  );
  return response.data;
};

export const renewTftData = async (
  gameName: string,
  tagLine: string
): Promise<TftRenewDto> => {
  const trimmedGameName = gameName.trim();
  const trimmedTagLine = tagLine.trim();

  const response = await api.get<TftRenewDto>(
    `/api/v1/tft/renew/${trimmedGameName}/${trimmedTagLine}`
  );
  return response.data;
};

export async function initializeOrGetTftBadges(
  gameName: string,
  tagLine: string
): Promise<TftBadgeDto[]> {
  const response = await fetch(
    `/api/v1/tft/badges/${encodeURIComponent(gameName)}/${encodeURIComponent(
      tagLine
    )}/initialize`,
    {
      method: "GET",
    }
  );

  if (!response.ok) {
    throw new Error("Failed to initialize or get badges");
  }

  return response.json();
}

// Error handling middleware
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.data?.message) {
      throw new Error(error.response.data.message);
    }
    throw error;
  }
);
