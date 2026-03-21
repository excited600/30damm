import apiClient from "../client";
import type { BlockUserRequest, BlockUserResponse } from "../types/block";

export const blockClient = {
  block(request: BlockUserRequest): Promise<BlockUserResponse> {
    return apiClient
      .post<BlockUserResponse>("/api/v1/blocks/users", request)
      .then((res) => res.data);
  },
};
