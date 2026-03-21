package beyondeyesight.domain.service.report

import beyondeyesight.domain.exception.ResourceNotFoundException
import beyondeyesight.domain.exception.report.DuplicateReportException
import beyondeyesight.domain.model.block.UserBlockedUserEntity
import beyondeyesight.domain.model.gathering.block.UserBlockedGatheringEntity
import beyondeyesight.domain.model.report.ReportEntity
import beyondeyesight.domain.model.report.ReportReason
import beyondeyesight.domain.model.report.ReportTargetType
import beyondeyesight.domain.repository.block.UserBlockedUserRepository
import beyondeyesight.domain.repository.gathering.block.UserBlockedGatheringRepository
import beyondeyesight.domain.repository.gathering.GatheringRepository
import beyondeyesight.domain.repository.report.ReportRepository
import beyondeyesight.domain.repository.user.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ReportService(
    private val reportRepository: ReportRepository,
    private val userRepository: UserRepository,
    private val gatheringRepository: GatheringRepository,
    private val userBlockedGatheringRepository: UserBlockedGatheringRepository,
    private val userBlockedUserRepository: UserBlockedUserRepository,
) {
    fun report(
        reporterUuid: UUID,
        targetType: ReportTargetType,
        targetUuid: UUID,
        reason: ReportReason,
        description: String?,
    ): ReportEntity {
        validateTargetExists(targetType, targetUuid)

        if (reportRepository.existsByReporterUuidAndTargetTypeAndTargetUuid(
                reporterUuid, targetType, targetUuid
            )
        ) {
            throw DuplicateReportException(
                targetType = targetType.name,
                targetUuid = targetUuid.toString(),
            )
        }

        val entity = ReportEntity.report(
            reporterUuid = reporterUuid,
            targetType = targetType,
            targetUuid = targetUuid,
            reason = reason,
            description = description,
        )

        val savedReport = reportRepository.save(entity)

        when (targetType) {
            ReportTargetType.GATHERING -> {
                userBlockedGatheringRepository.save(
                    UserBlockedGatheringEntity.create(
                        userUuid = reporterUuid,
                        gatheringUuid = targetUuid,
                    )
                )
            }
            ReportTargetType.USER -> {
                userBlockedUserRepository.save(
                    UserBlockedUserEntity.create(
                        blockerUuid = reporterUuid,
                        blockedUuid = targetUuid,
                    )
                )
            }
        }

        return savedReport
    }

    private fun validateTargetExists(targetType: ReportTargetType, targetUuid: UUID) {
        when (targetType) {
            ReportTargetType.USER -> {
                userRepository.findByUuid(targetUuid)
                    ?: throw ResourceNotFoundException.byUuid(
                        resourceName = "User",
                        resourceUuid = targetUuid,
                    )
            }
            ReportTargetType.GATHERING -> {
                gatheringRepository.findByUuid(targetUuid)
                    ?: throw ResourceNotFoundException.byUuid(
                        resourceName = "Gathering",
                        resourceUuid = targetUuid,
                    )
            }
        }
    }
}
