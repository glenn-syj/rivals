"use client";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { X } from "lucide-react";
import { useRivalryStore, type RivalryPlayer } from "@/store/rivalryStore";

interface TeamSelectionModalProps {
  player: RivalryPlayer | null;
  isOpen: boolean;
  onClose: () => void;
}

export default function TeamSelectionModal({
  player,
  isOpen,
  onClose,
}: TeamSelectionModalProps) {
  const { addPlayerToTeam, leftTeam, rightTeam, getTotalPlayerCount } =
    useRivalryStore();

  const handleAddToTeam = (team: "left" | "right") => {
    if (!player) return;

    const success = addPlayerToTeam(player, team);
    if (!success) {
      if (getTotalPlayerCount() >= 10) {
        alert("최대 10명까지만 추가할 수 있습니다");
      } else {
        alert("이미 해당 팀에 있는 플레이어입니다");
      }
      return;
    }

    alert(
      `${player.gameName}님이 ${
        team === "left" ? "LEFT" : "RIGHT"
      } 팀에 추가되었습니다!`
    );
    onClose();
  };

  if (!isOpen || !player) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* 배경 오버레이 */}
      <div
        className="absolute inset-0 bg-black/50 backdrop-blur-sm"
        onClick={onClose}
      />

      {/* 모달 */}
      <Card className="relative bg-slate-900 border-slate-700 shadow-2xl w-full max-w-md mx-4">
        <CardHeader className="pb-4">
          <div className="flex items-center justify-between">
            <CardTitle className="text-white">팀 선택</CardTitle>
            <Button
              variant="ghost"
              size="sm"
              onClick={onClose}
              className="text-gray-400 hover:text-white"
            >
              <X className="w-4 h-4" />
            </Button>
          </div>
          <div className="text-center">
            <p className="text-gray-300">
              <span className="font-semibold text-indigo-400">
                {player.gameName}#{player.tagLine}
              </span>
              을 어느 팀에 추가하시겠습니까?
            </p>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* 팀 선택 버튼들 */}
          <div className="grid grid-cols-2 gap-4">
            <Button
              onClick={() => handleAddToTeam("left")}
              className="h-20 flex flex-col items-center justify-center bg-indigo-600 hover:bg-indigo-700 text-white"
            >
              <div className="w-3 h-3 bg-indigo-400 rounded-full mb-2" />
              <span className="font-semibold">LEFT TEAM</span>
              <Badge
                variant="secondary"
                className="mt-1 bg-indigo-900/50 text-indigo-300"
              >
                {leftTeam.length}명
              </Badge>
            </Button>

            <Button
              onClick={() => handleAddToTeam("right")}
              className="h-20 flex flex-col items-center justify-center bg-red-600 hover:bg-red-700 text-white"
            >
              <div className="w-3 h-3 bg-red-400 rounded-full mb-2" />
              <span className="font-semibold">RIGHT TEAM</span>
              <Badge
                variant="secondary"
                className="mt-1 bg-red-900/50 text-red-300"
              >
                {rightTeam.length}명
              </Badge>
            </Button>
          </div>

          {/* 현재 상태 정보 */}
          <div className="text-center text-sm text-gray-400">
            <p>총 {getTotalPlayerCount()}/10명 • 각 팀 최소 1명 필요</p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
