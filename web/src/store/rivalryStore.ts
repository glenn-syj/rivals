import { create } from "zustand";
import { persist } from "zustand/middleware";

export interface RivalryPlayer {
  puuid: string;
  gameName: string;
  tagLine: string;
  id?: string;
}

interface RivalryState {
  leftTeam: RivalryPlayer[];
  rightTeam: RivalryPlayer[];
  isOpen: boolean;
}

interface RivalryActions {
  addPlayerToTeam: (player: RivalryPlayer, team: "left" | "right") => boolean;
  removePlayerFromTeam: (puuid: string, team: "left" | "right") => void;
  movePlayerToOtherTeam: (puuid: string, fromTeam: "left" | "right") => boolean;
  openRivalryCart: () => void;
  closeRivalryCart: () => void;
  clearRivalry: () => void;
  getTotalPlayerCount: () => number;
  canCreateRivalry: () => boolean;
}

export const useRivalryStore = create<RivalryState & RivalryActions>()(
  persist(
    (set, get) => ({
      // Initial State
      leftTeam: [],
      rightTeam: [],
      isOpen: false,

      // Actions
      addPlayerToTeam: (player, team) => {
        const { leftTeam, rightTeam } = get();
        const totalCount = leftTeam.length + rightTeam.length;

        if (totalCount >= 10) return false;

        const targetTeam = team === "left" ? leftTeam : rightTeam;
        if (targetTeam.some((p) => p.puuid === player.puuid)) {
          return false;
        }

        set((state) => ({
          [team === "left" ? "leftTeam" : "rightTeam"]: [
            ...state[team === "left" ? "leftTeam" : "rightTeam"],
            player,
          ],
        }));
        return true;
      },

      removePlayerFromTeam: (puuid, team) => {
        set((state) => ({
          [team === "left" ? "leftTeam" : "rightTeam"]: state[
            team === "left" ? "leftTeam" : "rightTeam"
          ].filter((p) => p.puuid !== puuid),
        }));
      },

      movePlayerToOtherTeam: (puuid, fromTeam) => {
        const { leftTeam, rightTeam } = get();
        const sourceTeam = fromTeam === "left" ? leftTeam : rightTeam;
        const targetTeam = fromTeam === "left" ? rightTeam : leftTeam;

        const player = sourceTeam.find((p) => p.puuid === puuid);
        if (!player) return false;
        if (targetTeam.some((p) => p.puuid === player.puuid)) return false;

        set((state) => ({
          [fromTeam === "left" ? "leftTeam" : "rightTeam"]: state[
            fromTeam === "left" ? "leftTeam" : "rightTeam"
          ].filter((p) => p.puuid !== puuid),
          [fromTeam === "left" ? "rightTeam" : "leftTeam"]: [
            ...state[fromTeam === "left" ? "rightTeam" : "leftTeam"],
            player,
          ],
        }));
        return true;
      },

      openRivalryCart: () => set({ isOpen: true }),
      closeRivalryCart: () => set({ isOpen: false }),
      clearRivalry: () => set({ leftTeam: [], rightTeam: [], isOpen: false }),

      // Derived State Getters
      getTotalPlayerCount: () => {
        const { leftTeam, rightTeam } = get();
        return leftTeam.length + rightTeam.length;
      },
      canCreateRivalry: () => {
        const { leftTeam, rightTeam } = get();
        return leftTeam.length >= 1 && rightTeam.length >= 1;
      },
    }),
    {
      name: "rivalry-storage",
      partialize: (state) => ({
        leftTeam: state.leftTeam,
        rightTeam: state.rightTeam,
      }),
    }
  )
);
