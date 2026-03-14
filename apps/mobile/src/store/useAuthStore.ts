import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";
import AsyncStorage from "@react-native-async-storage/async-storage";

interface AuthState {
  token: string | null;
  userUuid: string | null;
  _hasHydrated: boolean;
  setAuth: (token: string, userUuid: string) => void;
  isAuthenticated: () => boolean;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      userUuid: null,
      _hasHydrated: false,
      setAuth: (token, userUuid) => set({ token, userUuid }),
      isAuthenticated: () => get().token !== null,
      logout: () => set({ token: null, userUuid: null }),
    }),
    {
      name: "auth-storage",
      storage: createJSONStorage(() => AsyncStorage),
      partialize: (state) => ({ token: state.token, userUuid: state.userUuid }),
      onRehydrateStorage: () => () => {
        useAuthStore.setState({ _hasHydrated: true });
      },
    },
  ),
);
