package beyondeyesight.domain.model.report

import beyondeyesight.config.uuidV7
import beyondeyesight.domain.model.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "reports",
    uniqueConstraints = [
        UniqueConstraint(
            name = "unique_report_per_target",
            columnNames = ["reporter_uuid", "target_type", "target_uuid"]
        )
    ]
)
class ReportEntity(
    uuid: UUID,

    @Column(name = "reporter_uuid", nullable = false)
    val reporterUuid: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    val targetType: ReportTargetType,

    @Column(name = "target_uuid", nullable = false)
    val targetUuid: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val reason: ReportReason,

    @Column(nullable = true)
    val description: String?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ReportStatus,

    @Column(name = "reviewed_at", nullable = true)
    var reviewedAt: LocalDateTime?,

    @Column(name = "resolved_at", nullable = true)
    var resolvedAt: LocalDateTime?,
) : BaseEntity(uuid = uuid) {

    companion object {
        fun report(
            reporterUuid: UUID,
            targetType: ReportTargetType,
            targetUuid: UUID,
            reason: ReportReason,
            description: String?,
        ): ReportEntity {
            return ReportEntity(
                uuid = uuidV7(),
                reporterUuid = reporterUuid,
                targetType = targetType,
                targetUuid = targetUuid,
                reason = reason,
                description = description,
                status = ReportStatus.PENDING,
                reviewedAt = null,
                resolvedAt = null,
            )
        }

        const val RESOURCE_NAME = "reports"
    }
}
