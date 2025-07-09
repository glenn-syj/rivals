import { create } from "zustand";
import { BackendError } from "../lib/types";

interface ErrorState {
  error: BackendError | null;
  setError: (error: BackendError | null) => void;
}

export const useErrorStore = create<ErrorState>((set) => ({
  error: null,
  setError: (error) => set({ error }),
}));
