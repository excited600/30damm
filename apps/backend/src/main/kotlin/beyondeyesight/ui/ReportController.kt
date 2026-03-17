package beyondeyesight.ui

import beyondeyesight.api.ReportsApiService
import beyondeyesight.application.ReportApplicationService
import beyondeyesight.config.currentUserUuid
import beyondeyesight.domain.model.report.ReportReason
import beyondeyesight.domain.model.report.ReportTargetType
import beyondeyesight.model.ReportRequest
import beyondeyesight.model.ReportResponse
import org.springframework.stereotype.Service

@Service
class ReportController(
    private val reportApplicationService: ReportApplicationService,
) : ReportsApiService {

    override fun createReport(reportRequest: ReportRequest): ReportResponse {
        return reportApplicationService.report(
            reporterUuid = currentUserUuid(),
            targetType = ReportTargetType.valueOf(reportRequest.targetType.name),
            targetUuid = reportRequest.targetUuid,
            reason = ReportReason.valueOf(reportRequest.reason.name),
            description = reportRequest.description,
            mapper = { reportEntity ->
                ReportResponse(
                    reportUuid = reportEntity.uuid,
                    status = ReportResponse.Status.valueOf(reportEntity.status.name),
                )
            }
        )
    }
}
