import axios from "axios";
import type {
  ApiResponse,
  RiotAccountResponse,
  TftStatusDto,
  RivalryCreationDto,
  RivalryResultDto,
  RivalryDetailDto,
} from "./types";

const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://127.0.0.1:8080";

const api = axios.create({
  baseURL: API_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

// Riot Account APIs
export const findRiotAccount = async (
  gameName: string,
  tagLine: string
): Promise<ApiResponse<RiotAccountResponse>> => {
  const trimmedGameName = gameName.trim();
  const trimmedTagLine = tagLine.trim();

  const response = await api.get<ApiResponse<RiotAccountResponse>>(
    `/api/v1/riot/accounts/${trimmedGameName}/${trimmedTagLine}`
  );
  return response.data as ApiResponse<RiotAccountResponse>;
};

export const renewRiotAccount = async (gameName: string, tagLine: string) => {
  const response = await api.get<ApiResponse<RiotAccountResponse>>(
    `/api/v1/riot/accounts/renew/${gameName}/${tagLine}`
  );
  return response.data.data;
};

// TFT League Entry APIs
export const getTftStatus = async (
  gameName: string,
  tagLine: string
): Promise<ApiResponse<TftStatusDto>> => {
  const trimmedGameName = gameName.trim();
  const trimmedTagLine = tagLine.trim();

  const response = await api.get<ApiResponse<TftStatusDto>>(
    `/api/v1/tft/entries/${trimmedGameName}/${trimmedTagLine}`
  );
  return response.data as ApiResponse<TftStatusDto>;
};

// Rivalry APIs
export const createRivalry = async (creationDto: RivalryCreationDto) => {
  const response = await api.post<ApiResponse<RivalryResultDto>>(
    `/api/v1/rivalries`,
    JSON.stringify(creationDto)
  );
  return response.data as ApiResponse<RivalryResultDto>;
};

export const getRivalryById = async (rivalryId: string) => {
  const response = await api.get<ApiResponse<RivalryDetailDto>>(
    `/api/v1/rivalries/${rivalryId}`
  );
  return response.data as ApiResponse<RivalryDetailDto>;
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
