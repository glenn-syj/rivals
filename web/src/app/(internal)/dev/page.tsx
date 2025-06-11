// app/(internal)/dev/page.tsx
"use client";

import { useState } from "react";
import { getTftMatches } from "@/lib/api";
import { TftRecentMatchDto } from "@/lib/types";

export default function DevPage() {
  const [gameName, setGameName] = useState("");
  const [tagLine, setTagLine] = useState("");
  const [matches, setMatches] = useState<TftRecentMatchDto[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const data = await getTftMatches(gameName, tagLine);
      setMatches(data);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "매치 조회 중 오류가 발생했습니다."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold mb-6">TFT Match API Test</h1>

      <form onSubmit={handleSubmit} className="mb-8 space-y-4">
        <div>
          <label className="block text-sm font-medium mb-1">Game Name</label>
          <input
            type="text"
            value={gameName}
            onChange={(e) => setGameName(e.target.value)}
            className="w-full px-3 py-2 border rounded-md"
            placeholder="Enter game name"
          />
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">Tag Line</label>
          <input
            type="text"
            value={tagLine}
            onChange={(e) => setTagLine(e.target.value)}
            className="w-full px-3 py-2 border rounded-md"
            placeholder="Enter tag line"
          />
        </div>
        <button
          type="submit"
          disabled={loading}
          className="w-full bg-blue-500 text-white py-2 rounded-md hover:bg-blue-600 disabled:bg-blue-300"
        >
          {loading ? "Loading..." : "Get Matches"}
        </button>
      </form>

      {error && (
        <div className="mb-6 p-4 bg-red-100 text-red-700 rounded-md">
          {error}
        </div>
      )}

      {matches.length > 0 && (
        <div className="space-y-4">
          <h2 className="text-xl font-semibold">Recent Matches</h2>
          {matches.map((match) => (
            <div key={match.id} className="border rounded-md p-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p>
                    <span className="font-medium">Match ID:</span>{" "}
                    {match.matchId}
                  </p>
                  <p>
                    <span className="font-medium">Game Creation:</span>{" "}
                    {new Date(match.gameCreation).toLocaleString()}
                  </p>
                  <p>
                    <span className="font-medium">Game Length:</span>{" "}
                    {Math.floor(match.gameLength / 60)}분{" "}
                    {Math.floor(match.gameLength % 60)}초
                  </p>
                  <p>
                    <span className="font-medium">Level:</span> {match.level}
                  </p>
                  <p>
                    <span className="font-medium">Queue Type:</span>{" "}
                    {match.queueType}
                  </p>
                </div>
                <div>
                  <p className="font-medium mb-2">Traits:</p>
                  <div className="text-sm">
                    {match.traits.map((trait, index) => (
                      <p key={index}>
                        {trait.name} (Level {trait.tier_current}/
                        {trait.tier_total})
                      </p>
                    ))}
                  </div>
                </div>
              </div>
              <div className="mt-4">
                <p className="font-medium mb-2">Units:</p>
                <div className="grid grid-cols-4 gap-2">
                  {match.units.map((unit, index) => (
                    <div key={index} className="text-sm">
                      <p>
                        <span className="font-medium">Character ID:</span>{" "}
                        {unit.character_id}
                      </p>
                      <p>
                        {unit.name} (★{unit.tier})
                      </p>
                      {unit.itemNames.length > 0 && (
                        <p className="text-gray-600">
                          Items: {unit.itemNames.join(", ")}
                        </p>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
