import axios from "axios";
import type {
  ApiResponse,
  RiotAccountResponse,
  TftStatusDto,
  RivalryCreationDto,
  RivalryResultDto,
  RivalryDetailDto,
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
): Promise<RiotAccountResponse> => {
  const trimmedGameName = gameName.trim();
  const trimmedTagLine = tagLine.trim();

  const response = await api.get<RiotAccountResponse>(
    `/api/v1/riot/accounts/${trimmedGameName}/${trimmedTagLine}`
  );
  return response.data;
};

export const renewRiotAccount = async (
  gameName: string,
  tagLine: string
): Promise<RiotAccountResponse> => {
  const trimmedGameName = gameName.trim();
  const trimmedTagLine = tagLine.trim();

  const response = await api.get<RiotAccountResponse>(
    `/api/v1/riot/accounts/renew/${trimmedGameName}/${trimmedTagLine}`
  );
  return response.data;
};

// TFT League Entry APIs
export const getTftStatus = async (
  gameName: string,
  tagLine: string
): Promise<TftStatusDto> => {
  const trimmedGameName = gameName.trim();
  const trimmedTagLine = tagLine.trim();

  const response = await api.get<TftStatusDto>(
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
