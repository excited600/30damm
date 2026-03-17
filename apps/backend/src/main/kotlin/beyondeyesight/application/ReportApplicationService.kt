package beyondeyesight.application

import beyondeyesight.domain.model.report.ReportEntity
import beyondeyesight.domain.model.report.ReportReason
import beyondeyesight.domain.model.report.ReportTargetType
import beyondeyesight.domain.service.report.ReportService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ReportApplicationService(
    private val reportService: ReportService,
) {
    @Transactional
    fun <R> report(
        reporterUuid: UUID,
        targetType: ReportTargetType,
        targetUuid: UUID,
        reason: ReportReason,
        description: String?,
        mapper: (ReportEntity) -> R,
    ): R {
        val reportEntity = reportService.report(
            reporterUuid = reporterUuid,
            targetType = targetType,
            targetUuid = targetUuid,
            reason = reason,
            description = description,
        )
        return mapper.invoke(reportEntity)
    }
}
