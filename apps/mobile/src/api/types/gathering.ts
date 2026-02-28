export interface Gathering {
  id: number;
  title: string;
  description: string;
  maxParticipants: number;
  currentParticipants: number;
  startAt: string;
  endAt: string;
  location: string;
  status: GatheringStatus;
  createdAt: string;
}

export type GatheringStatus = "RECRUITING" | "FULL" | "IN_PROGRESS" | "COMPLETED" | "CANCELLED";

export interface GatheringFilters {
  status?: GatheringStatus;
  keyword?: string;
  page?: number;
  size?: number;
}
