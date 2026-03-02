import { create } from "zustand";
import type { GatheringCategory } from "@/api/types/gathering";

interface CreateGatheringState {
  minCapacity: number;
  maxCapacity: number;
  isGenderRatioEnabled: boolean;
  maxMaleCapacity: number | null;
  maxFemaleCapacity: number | null;
  isFree: boolean;
  price: number | null;
  isSplit: boolean;
  title: string;
  description: string;
  category: GatheringCategory;
  location: string | null;
  date: string | null;
  startTime: string | null;
  duration: number | null;

  setParticipants: (min: number, max: number) => void;
  setGenderRatio: (
    enabled: boolean,
    maxMale: number | null,
    maxFemale: number | null,
  ) => void;
  setPrice: (isFree: boolean, price: number | null, isSplit: boolean) => void;
  setCategory: (category: GatheringCategory) => void;
  setIntroduction: (title: string, description: string) => void;
  setLocation: (location: string | null) => void;
  setWhen: (
    date: string | null,
    startTime: string | null,
    duration: number | null,
  ) => void;
  reset: () => void;
}

const initialState = {
  minCapacity: 2,
  maxCapacity: 10,
  isGenderRatioEnabled: false,
  maxMaleCapacity: null,
  maxFemaleCapacity: null,
  isFree: true,
  price: null,
  isSplit: false,
  title: "",
  description: "",
  category: "NONE" as GatheringCategory,
  location: null,
  date: null,
  startTime: null,
  duration: null,
};

export const useCreateGatheringStore = create<CreateGatheringState>()(
  (set) => ({
    ...initialState,
    setParticipants: (min, max) =>
      set({ minCapacity: min, maxCapacity: max }),
    setGenderRatio: (enabled, maxMale, maxFemale) =>
      set({
        isGenderRatioEnabled: enabled,
        maxMaleCapacity: maxMale,
        maxFemaleCapacity: maxFemale,
      }),
    setPrice: (isFree, price, isSplit) => set({ isFree, price, isSplit }),
    setCategory: (category) => set({ category }),
    setIntroduction: (title, description) => set({ title, description }),
    setLocation: (location) => set({ location }),
    setWhen: (date, startTime, duration) =>
      set({ date, startTime, duration }),
    reset: () => set(initialState),
  }),
);
