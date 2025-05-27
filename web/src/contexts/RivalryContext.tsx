"use client";

import { createContext, useContext, useState, type ReactNode } from "react";

export interface Player {
  puuid: string;
  gameName: string;
  tagLine: string;
  id?: string;
}

export interface RivalryState {
  leftTeam: Player[];
  rightTeam: Player[];
  isOpen: boolean;
}

interface RivalryContextType {
  rivalry: RivalryState;
  addPlayerToTeam: (player: Player, team: "left" | "right") => boolean;
  removePlayerFromTeam: (puuid: string, team: "left" | "right") => void;
  movePlayerToOtherTeam: (puuid: string, fromTeam: "left" | "right") => boolean;
  openRivalryCart: () => void;
  closeRivalryCart: () => void;
  clearRivalry: () => void;
  canCreateRivalry: () => boolean;
  getTotalPlayerCount: () => number;
}

const RivalryContext = createContext<RivalryContextType | undefined>(undefined);

export function RivalryProvider({ children }: { children: ReactNode }) {
  const [rivalry, setRivalry] = useState<RivalryState>({
    leftTeam: [],
    rightTeam: [],
    isOpen: false,
  });

  const addPlayerToTeam = (player: Player, team: "left" | "right"): boolean => {
    const targetTeam = team === "left" ? rivalry.leftTeam : rivalry.rightTeam;
    const totalCount = rivalry.leftTeam.length + rivalry.rightTeam.length;

    // 총 10명 제한 확인
    if (totalCount >= 10) return false;

    // 같은 팀에 동일한 계정이 있는지 확인
    if (targetTeam.some((p) => p.puuid === player.puuid)) return false;

    setRivalry((prev) => ({
      ...prev,
      [team === "left" ? "leftTeam" : "rightTeam"]: [...targetTeam, player],
    }));

    return true;
  };

  const removePlayerFromTeam = (puuid: string, team: "left" | "right") => {
    setRivalry((prev) => ({
      ...prev,
      [team === "left" ? "leftTeam" : "rightTeam"]: prev[
        team === "left" ? "leftTeam" : "rightTeam"
      ].filter((p) => p.puuid !== puuid),
    }));
  };

  const movePlayerToOtherTeam = (
    puuid: string,
    fromTeam: "left" | "right"
  ): boolean => {
    const toTeam = fromTeam === "left" ? "right" : "left";
    const sourceTeam =
      fromTeam === "left" ? rivalry.leftTeam : rivalry.rightTeam;
    const targetTeam =
      fromTeam === "left" ? rivalry.rightTeam : rivalry.leftTeam;

    const player = sourceTeam.find((p) => p.puuid === puuid);
    if (!player) return false;

    // 대상 팀에 동일한 계정이 있는지 확인
    if (targetTeam.some((p) => p.puuid === player.puuid)) return false;

    // 이동 실행
    removePlayerFromTeam(puuid, fromTeam);
    return addPlayerToTeam(player, toTeam);
  };

  const openRivalryCart = () => {
    setRivalry((prev) => ({ ...prev, isOpen: true }));
  };

  const closeRivalryCart = () => {
    setRivalry((prev) => ({ ...prev, isOpen: false }));
  };

  const clearRivalry = () => {
    setRivalry({
      leftTeam: [],
      rightTeam: [],
      isOpen: false,
    });
  };

  const canCreateRivalry = (): boolean => {
    return rivalry.leftTeam.length >= 1 && rivalry.rightTeam.length >= 1;
  };

  const getTotalPlayerCount = (): number => {
    return rivalry.leftTeam.length + rivalry.rightTeam.length;
  };

  return (
    <RivalryContext.Provider
      value={{
        rivalry,
        addPlayerToTeam,
        removePlayerFromTeam,
        movePlayerToOtherTeam,
        openRivalryCart,
        closeRivalryCart,
        clearRivalry,
        canCreateRivalry,
        getTotalPlayerCount,
      }}
    >
      {children}
    </RivalryContext.Provider>
  );
}

export function useRivalry() {
  const context = useContext(RivalryContext);
  if (context === undefined) {
    throw new Error("useRivalry must be used within a RivalryProvider");
  }
  return context;
}
