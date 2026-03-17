export type ReportTargetType = "USER" | "GATHERING";

export type ReportReason = "OFFENSIVE_CONTENT" | "ILLEGAL_OR_FALSE_INFO" | "OTHER";

export type ReportStatus = "PENDING" | "REVIEWED" | "RESOLVED" | "DISMISSED";

export interface CreateReportRequest {
  targetType: ReportTargetType;
  targetUuid: string;
  reason: ReportReason;
  description?: string;
}

export interface CreateReportResponse {
  reportUuid: string;
  status: ReportStatus;
}
