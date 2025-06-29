import { create } from "zustand";

type LoadingStatus = "idle" | "loading" | "success" | "error";

interface SummonerPageLoadState {
  accountStatus: LoadingStatus;
  statusStatus: LoadingStatus;
  matchStatus: LoadingStatus;
  badgesStatus: LoadingStatus;
  isInitialRefreshPending: boolean;
  setAccountStatus: (status: LoadingStatus) => void;
  setStatusStatus: (status: LoadingStatus) => void;
  setMatchStatus: (status: LoadingStatus) => void;
  setBadgesStatus: (status: LoadingStatus) => void;
  setIsInitialRefreshPending: (isPending: boolean) => void;
  isAllDataLoaded: () => boolean;
  resetLoadStatus: () => void;
}

export const useSummonerPageLoadStore = create<SummonerPageLoadState>(
  (set, get) => ({
    accountStatus: "idle",
    statusStatus: "idle",
    matchStatus: "idle",
    badgesStatus: "idle",
    isInitialRefreshPending: false,
    setAccountStatus: (status) => set({ accountStatus: status }),
    setStatusStatus: (status) => set({ statusStatus: status }),
    setMatchStatus: (status) => set({ matchStatus: status }),
    setBadgesStatus: (status) => set({ badgesStatus: status }),
    setIsInitialRefreshPending: (isPending) =>
      set({ isInitialRefreshPending: isPending }),
    isAllDataLoaded: () => {
      const state = get();
      // 'success' or 'error' are considered loaded states
      const isLoaded = (status: LoadingStatus) =>
        status === "success" || status === "error";

      return (
        isLoaded(state.accountStatus) &&
        isLoaded(state.statusStatus) &&
        isLoaded(state.matchStatus) &&
        isLoaded(state.badgesStatus)
      );
    },
    resetLoadStatus: () =>
      set({
        accountStatus: "idle",
        statusStatus: "idle",
        matchStatus: "idle",
        badgesStatus: "idle",
        isInitialRefreshPending: false,
      }),
  })
);
