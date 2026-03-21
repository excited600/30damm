import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";
import AsyncStorage from "@react-native-async-storage/async-storage";

interface AuthState {
  token: string | null;
  refreshToken: string | null;
  userUuid: string | null;
  _hasHydrated: boolean;
  setAuth: (token: string, refreshToken: string, userUuid: string) => void;
  setTokens: (token: string, refreshToken: string) => void;
  isAuthenticated: () => boolean;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      refreshToken: null,
      userUuid: null,
      _hasHydrated: false,
      setAuth: (token, refreshToken, userUuid) =>
        set({ token, refreshToken, userUuid }),
      setTokens: (token, refreshToken) => set({ token, refreshToken }),
      isAuthenticated: () => get().token !== null,
      logout: () => set({ token: null, refreshToken: null, userUuid: null }),
    }),
    {
      name: "auth-storage",
      storage: createJSONStorage(() => AsyncStorage),
      partialize: (state) => ({
        token: state.token,
        refreshToken: state.refreshToken,
        userUuid: state.userUuid,
      }),
      onRehydrateStorage: () => () => {
        useAuthStore.setState({ _hasHydrated: true });
      },
    },
  ),
);
