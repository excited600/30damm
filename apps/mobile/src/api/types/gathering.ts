export type GatheringCategory = "NONE" | "PARTY" | "FOOD_DRINK" | "ACTIVITY";

export type Gender = "MALE" | "FEMALE";

export type ViewerRelation = "SELF" | "STRANGER" | "BLOCKED";

export interface OpenGatheringRequest {
  title: string;
  description: string;
  category: GatheringCategory;
  location?: string | null;
  date?: string | null;
  startTime?: string | null;
  duration?: number | null;
  minCapacity: number;
  maxCapacity: number;
  isGenderRatioEnabled: boolean;
  maxMaleCapacity?: number | null;
  maxFemaleCapacity?: number | null;
  isFree: boolean;
  price?: number | null;
  isSplit: boolean;
}

export interface OpenGatheringResponse {
  gatheringUuid: string;
}

export interface ScrollFilteredGatheringsParams {
  uuid?: string;
  score?: number;
  size: number;
  statuses?: string[];
  categories?: string[];
}

export interface GatheringListItem {
  gatheringUuid: string;
  imgUrl?: string | null;
  title: string;
  location?: string | null;
  date?: string | null;
  startTime?: string | null;
  duration?: number | null;
  maleCount: number;
  femaleCount: number;
  host: {
    userUuid: string;
    nickname: string;
    profileImageUrl?: string | null;
    gender: Gender;
    viewerRelation: ViewerRelation;
  };
  isFree: boolean;
  isSplit: boolean;
  price?: number | null;
}

export interface ScrollFilteredGatheringsResponse {
  cursor?: {
    score: number;
    uuid: string;
  };
  hasNext: boolean;
  list: GatheringListItem[];
}

export interface GatheringDetailResponse {
  gatheringUuid: string;
  imgUrl?: string | null;
  title: string;
  description: string;
  host: {
    userUuid: string;
    nickname: string;
    profileImageUrl?: string | null;
    gender: Gender;
    viewerRelation: ViewerRelation;
  };
  guests: Array<{
    userUuid: string;
    nickname: string;
    profileImageUrl?: string | null;
    gender: Gender;
    viewerRelation: ViewerRelation;
  }>;
  category: GatheringCategory;
  minCapacity: number;
  maxCapacity: number;
  isGenderRatioEnabled: boolean;
  maxMaleCapacity?: number | null;
  maxFemaleCapacity?: number | null;
  currentMaleCount: number;
  currentFemaleCount: number;
  date?: string | null;
  startTime?: string | null;
  duration?: number | null;
  location?: string | null;
  isFree: boolean;
  isSplit: boolean;
  price?: number | null;
  userStatus: GatheringUserStatus;
}

export type GatheringUserStatus = "HOST_OPENED" | "GUEST_JOINED" | "GUEST_NOT_JOINED";
