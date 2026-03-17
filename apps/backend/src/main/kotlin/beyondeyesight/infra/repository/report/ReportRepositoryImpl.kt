package beyondeyesight.infra.repository.report

import beyondeyesight.domain.model.report.ReportEntity
import beyondeyesight.domain.model.report.ReportTargetType
import beyondeyesight.domain.repository.report.ReportRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class ReportRepositoryImpl(
    private val reportJpaRepository: ReportJpaRepository,
) : ReportRepository {

    override fun save(reportEntity: ReportEntity): ReportEntity {
        return reportJpaRepository.save(reportEntity)
    }

    override fun existsByReporterUuidAndTargetTypeAndTargetUuid(
        reporterUuid: UUID,
        targetType: ReportTargetType,
        targetUuid: UUID,
    ): Boolean {
        return reportJpaRepository.existsByReporterUuidAndTargetTypeAndTargetUuid(
            reporterUuid, targetType, targetUuid
        )
    }
}
