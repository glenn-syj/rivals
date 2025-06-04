"use client";

import { useState } from "react";

interface TftStatusResponse {
  queueType: string;
  tier: string;
  rank: string;
  leaguePoints: number;
  wins: number;
  losses: number;
}

const formatQueueType = (queueType: string): string => {
  switch (queueType) {
    case "RANKED_TFT":
      return "일반 랭크";
    case "RANKED_TFT_TURBO":
      return "하이퍼롤";
    case "RANKED_TFT_DOUBLE_UP":
      return "더블업";
    default:
      return queueType;
  }
};

export default function DevPage() {
  const [gameName, setGameName] = useState("");
  const [tagLine, setTagLine] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [results, setResults] = useState<TftStatusResponse[] | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setResults(null);

    try {
      const response = await fetch(
        `http://127.0.0.1:8080/api/v1/tft/entries/internal/${encodeURIComponent(
          gameName
        )}/${encodeURIComponent(tagLine)}`
      );

      if (!response.ok) {
        throw new Error("계정을 찾을 수 없거나 TFT 기록이 존재하지 않습니다.");
      }

      const data = await response.json();
      setResults(data);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "알 수 없는 오류가 발생했습니다."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="p-8 max-w-2xl mx-auto">
      <h1 className="text-2xl font-bold mb-6">TFT 상태 조회 테스트</h1>

      <form onSubmit={handleSubmit} className="space-y-4 mb-8">
        <div>
          <label htmlFor="gameName" className="block text-sm font-medium mb-1">
            게임 이름
          </label>
          <input
            id="gameName"
            type="text"
            value={gameName}
            onChange={(e) => setGameName(e.target.value)}
            className="w-full p-2 border rounded"
            required
          />
        </div>

        <div>
          <label htmlFor="tagLine" className="block text-sm font-medium mb-1">
            태그라인
          </label>
          <input
            id="tagLine"
            type="text"
            value={tagLine}
            onChange={(e) => setTagLine(e.target.value)}
            className="w-full p-2 border rounded"
            required
          />
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full bg-blue-500 text-white py-2 px-4 rounded hover:bg-blue-600 disabled:bg-blue-300"
        >
          {loading ? "조회 중..." : "조회하기"}
        </button>
      </form>

      {error && (
        <div className="p-4 bg-red-100 text-red-700 rounded mb-4">{error}</div>
      )}

      {results && (
        <div className="space-y-4">
          {results.map((result, index) => (
            <div key={index} className="p-4 bg-gray-100 rounded">
              <div className="flex justify-between items-center mb-2">
                <h3 className="font-bold">
                  {formatQueueType(result.queueType)}
                </h3>
                <span className="text-sm text-gray-500">
                  {result.queueType}
                </span>
              </div>
              <div className="grid grid-cols-2 gap-2 text-sm">
                <div>
                  티어: {result.tier} {result.rank}
                </div>
                <div>LP: {result.leaguePoints}</div>
                <div>승리: {result.wins}</div>
                <div>패배: {result.losses}</div>
                <div>
                  승률:{" "}
                  {(
                    (result.wins / (result.wins + result.losses)) *
                    100
                  ).toFixed(1)}
                  %
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
