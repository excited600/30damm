package beyondeyesight.infra.repository.report

import beyondeyesight.domain.model.report.ReportEntity
import beyondeyesight.domain.model.report.ReportTargetType
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ReportJpaRepository : JpaRepository<ReportEntity, UUID> {
    fun existsByReporterUuidAndTargetTypeAndTargetUuid(
        reporterUuid: UUID,
        targetType: ReportTargetType,
        targetUuid: UUID,
    ): Boolean
}
