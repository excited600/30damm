import { create } from "zustand";

interface AppState {
  isOnboarded: boolean;
  setOnboarded: (value: boolean) => void;
}

export const useAppStore = create<AppState>()((set) => ({
  isOnboarded: false,
  setOnboarded: (value) => set({ isOnboarded: value }),
}));
