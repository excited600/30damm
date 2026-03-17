package beyondeyesight.domain.repository.report

import beyondeyesight.domain.model.report.ReportEntity
import beyondeyesight.domain.model.report.ReportTargetType
import java.util.UUID

interface ReportRepository {
    fun save(reportEntity: ReportEntity): ReportEntity
    fun existsByReporterUuidAndTargetTypeAndTargetUuid(
        reporterUuid: UUID,
        targetType: ReportTargetType,
        targetUuid: UUID,
    ): Boolean
}
