"use client";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { X, ArrowLeftRight, Users, Trash2 } from "lucide-react";
import { useRivalry, type Player } from "@/contexts/RivalryContext";
import { createRivalry } from "@/lib/api";
import type { RivalryCreationDto, RivalryResultDto } from "@/lib/types";
import { useRouter } from "next/navigation";

interface PlayerCardProps {
  player: Player;
  team: "left" | "right";
  onRemove: () => void;
  onMove: () => void;
  canMove: boolean;
}

function PlayerCard({
  player,
  team,
  onRemove,
  onMove,
  canMove,
}: PlayerCardProps) {
  return (
    <div className="flex items-center justify-between p-3 bg-slate-700/50 rounded-lg border border-slate-600">
      <div className="flex-1">
        <p className="text-white font-medium">{player.gameName}</p>
        <p className="text-gray-400 text-sm">#{player.tagLine}</p>
      </div>
      <div className="flex gap-2">
        <Button
          size="sm"
          variant="ghost"
          onClick={onMove}
          disabled={!canMove}
          className="text-indigo-400 hover:text-indigo-300 hover:bg-indigo-900/20 disabled:opacity-50"
        >
          <ArrowLeftRight className="w-4 h-4" />
        </Button>
        <Button
          size="sm"
          variant="ghost"
          onClick={onRemove}
          className="text-red-400 hover:text-red-300 hover:bg-red-900/20"
        >
          <Trash2 className="w-4 h-4" />
        </Button>
      </div>
    </div>
  );
}

function TeamSection({
  title,
  team,
  players,
}: {
  title: string;
  team: "left" | "right";
  players: Player[];
}) {
  const {
    removePlayerFromTeam,
    movePlayerToOtherTeam,
    rivalry,
    canCreateRivalry,
  } = useRivalry();

  const canMovePlayer = (puuid: string): boolean => {
    const otherTeam = team === "left" ? rivalry.rightTeam : rivalry.leftTeam;
    return !otherTeam.some((p) => p.puuid === puuid);
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-lg font-semibold text-white">{title}</h3>
        <Badge variant="secondary" className="bg-slate-800/80 text-slate-300">
          {players.length}명
        </Badge>
      </div>

      {/* 플레이어 목록 */}
      <div className="space-y-2 max-h-60 overflow-y-auto">
        {players.length === 0 ? (
          <div className="text-center py-8 text-gray-400">
            <Users className="w-8 h-8 mx-auto mb-2 opacity-50" />
            <p>검색해서 플레이어를 추가해보세요</p>
          </div>
        ) : (
          players.map((player) => (
            <PlayerCard
              key={player.puuid}
              player={player}
              team={team}
              onRemove={() => removePlayerFromTeam(player.puuid, team)}
              onMove={() => movePlayerToOtherTeam(player.puuid, team)}
              canMove={canMovePlayer(player.puuid)}
            />
          ))
        )}
      </div>
    </div>
  );
}

export default function RivalryCart() {
  const {
    rivalry,
    closeRivalryCart,
    clearRivalry,
    canCreateRivalry,
    getTotalPlayerCount,
  } = useRivalry();
  const router = useRouter();

  const handleCreateRivalry = async () => {
    if (!canCreateRivalry()) return;

    const rivalryCreationDto: RivalryCreationDto = {
      participants: [
        ...rivalry.leftTeam.map((p) => ({
          id: String(p.id),
          side: "LEFT" as const,
        })),
        ...rivalry.rightTeam.map((p) => ({
          id: String(p.id),
          side: "RIGHT" as const,
        })),
      ],
    };

    createRivalry(rivalryCreationDto)
      .then((result) => {
        console.log("Rivalry created:", result);
        const rivalryId = String(result.rivalryId);
        clearRivalry();
        closeRivalryCart();
        router.push(`/rivalry/${rivalryId}`);
      })
      .catch((err) => {
        console.error("Error creating rivalry:", err);
        alert("라이벌리 생성에 실패했습니다");
      });
  };

  if (!rivalry.isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex">
      {/* 배경 오버레이 */}
      <div
        className="flex-1 bg-black/50 backdrop-blur-sm"
        onClick={closeRivalryCart}
      />

      {/* 슬라이드 패널 */}
      <div className="w-full max-w-2xl bg-slate-900 border-l border-slate-700 shadow-2xl overflow-hidden">
        <div className="flex flex-col h-full">
          {/* 헤더 */}
          <div className="flex items-center justify-between p-6 border-b border-slate-700">
            <div>
              <h2 className="text-2xl font-bold text-white">라이벌리 생성</h2>
              <p className="text-gray-400 mt-1">
                총 {getTotalPlayerCount()}/10명 • 각 팀 최소 1명 필요
              </p>
            </div>
            <Button
              variant="ghost"
              size="sm"
              onClick={closeRivalryCart}
              className="text-gray-400 hover:text-white"
            >
              <X className="w-5 h-5" />
            </Button>
          </div>

          {/* 콘텐츠 */}
          <div className="flex-1 overflow-y-auto p-6">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              {/* LEFT TEAM */}
              <Card className="bg-slate-800/50 border-slate-700">
                <CardHeader className="pb-4">
                  <CardTitle className="text-indigo-400 flex items-center">
                    <div className="w-3 h-3 bg-indigo-500 rounded-full mr-2" />
                    LEFT TEAM
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <TeamSection
                    title=""
                    team="left"
                    players={rivalry.leftTeam}
                  />
                </CardContent>
              </Card>

              {/* RIGHT TEAM */}
              <Card className="bg-slate-800/50 border-slate-700">
                <CardHeader className="pb-4">
                  <CardTitle className="text-red-400 flex items-center">
                    <div className="w-3 h-3 bg-red-500 rounded-full mr-2" />
                    RIGHT TEAM
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <TeamSection
                    title=""
                    team="right"
                    players={rivalry.rightTeam}
                  />
                </CardContent>
              </Card>
            </div>
          </div>

          {/* 푸터 */}
          <div className="p-6 border-t border-slate-700 bg-slate-900/50">
            <div className="flex gap-3">
              <Button
                variant="outline"
                onClick={clearRivalry}
                className="flex-1 border-slate-600 text-slate-300 hover:bg-slate-800"
              >
                초기화
              </Button>
              <Button
                onClick={handleCreateRivalry}
                disabled={!canCreateRivalry()}
                className="flex-1 bg-gradient-to-r from-slate-600 to-indigo-600 hover:from-slate-700 hover:to-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                라이벌리 생성
              </Button>
            </div>

            {!canCreateRivalry() && (
              <p className="text-red-400 text-sm mt-2 text-center">
                각 팀에 최소 1명씩 추가해주세요
              </p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
