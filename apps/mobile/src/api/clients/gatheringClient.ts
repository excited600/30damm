import apiClient from "../client";
import type {
  OpenGatheringRequest,
  OpenGatheringResponse,
  ScrollFilteredGatheringsParams,
  ScrollFilteredGatheringsResponse,
  GatheringDetailResponse,
} from "../types/gathering";

export const gatheringClient = {
  open(request: OpenGatheringRequest): Promise<OpenGatheringResponse> {
    return apiClient
      .post<OpenGatheringResponse>("/api/v1/gatherings", request)
      .then((res) => res.data);
  },

  scroll(
    params: ScrollFilteredGatheringsParams,
  ): Promise<ScrollFilteredGatheringsResponse> {
    return apiClient
      .get<ScrollFilteredGatheringsResponse>("/api/v1/gatherings", { params })
      .then((res) => res.data);
  },

  getDetail(gatheringUuid: string): Promise<GatheringDetailResponse> {
    return apiClient
      .get<GatheringDetailResponse>(`/api/v1/gatherings/${gatheringUuid}`)
      .then((res) => res.data);
  },

  join(gatheringUuid: string): Promise<void> {
    return apiClient
      .post(`/api/v1/gatherings/${gatheringUuid}/join`, {})
      .then(() => undefined);
  },
};
