import { create } from "zustand";

interface RegisterState {
  email: string;
  password: string;
  setCredentials: (email: string, password: string) => void;
  reset: () => void;
}

export const useRegisterStore = create<RegisterState>()((set) => ({
  email: "",
  password: "",
  setCredentials: (email, password) => set({ email, password }),
  reset: () => set({ email: "", password: "" }),
}));
