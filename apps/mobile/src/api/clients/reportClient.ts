import apiClient from "../client";
import type { CreateReportRequest, CreateReportResponse } from "../types/report";

export const reportClient = {
  report(request: CreateReportRequest): Promise<CreateReportResponse> {
    return apiClient
      .post<CreateReportResponse>("/api/v1/reports", request)
      .then((res) => res.data);
  },
};
