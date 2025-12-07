package beyondeyesight.domain.service.gathering

import beyondeyesight.config.isThirtyMinuteInterval
import beyondeyesight.domain.exception.InvalidValueException
import beyondeyesight.domain.exception.ResourceNotFoundException
import beyondeyesight.domain.model.UserEntity
import beyondeyesight.domain.model.gathering.DateSchedule
import beyondeyesight.domain.model.gathering.GatheringEntity
import beyondeyesight.domain.model.gathering.ScheduleType
import beyondeyesight.domain.model.gathering.SeriesEntity
import beyondeyesight.domain.model.gathering.SeriesScheduleEntity
import beyondeyesight.domain.model.gathering.WeeklySchedule
import beyondeyesight.domain.repository.ParticipantRepository
import beyondeyesight.domain.repository.UserRepository
import beyondeyesight.domain.repository.gathering.GatheringRepository
import beyondeyesight.domain.repository.gathering.SeriesRepository
import beyondeyesight.domain.repository.gathering.SeriesScheduleRepository
import beyondeyesight.domain.service.LockService
import beyondeyesight.domain.service.ParticipantService
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

@Service
class GatheringService(
    private val gatheringRepository: GatheringRepository,
    private val participantService: ParticipantService,
    private val participantRepository: ParticipantRepository,
    private val lockService: LockService,
    private val userRepository: UserRepository,
    private val seriesRepository: SeriesRepository,
    private val seriesScheduleRepository: SeriesScheduleRepository,
) {

    fun scheduleSeries() {

    }

    fun open(
        hostUuid: UUID,
        approveType: GatheringEntity.ApproveType,
        minCapacity: Int,
        maxCapacity: Int,
        genderRatioEnabled: Boolean,
        minAge: Int,
        maxAge: Int,
        maxMaleCount: Int?,
        maxFemaleCount: Int?,
        fee: Int,
        discountEnabled: Boolean,
        offline: Boolean,
        place: String,
        category: GatheringEntity.Category,
        subCategory: GatheringEntity.SubCategory,
        imageUrl: String,
        title: String,
        introduction: String,
        startDateTime: LocalDateTime,
        duration: Duration?,
    ): GatheringEntity {
        validate(minAge, maxAge, maxMaleCount, maxFemaleCount, fee)

        val host = userRepository.findByUuid(hostUuid) ?: throw ResourceNotFoundException(
            resourceName = "User",
            resourceId = hostUuid
        )

        val (currentMaleCount, currentFemaleCount) =
            if (host.gender == UserEntity.Gender.M) {
               1 to 0
            } else {
                0 to 1
            }
        val entity = GatheringEntity.Companion.open(
            hostUuid = host.uuid,
            approveType = approveType,
            minCapacity = minCapacity,
            maxCapacity = maxCapacity,
            genderRatioEnabled = genderRatioEnabled,
            minAge = minAge,
            maxAge = maxAge,
            maxMaleCount = maxMaleCount,
            maxFemaleCount = maxFemaleCount,
            currentMaleCount = currentMaleCount,
            currentFemaleCount = currentFemaleCount,
            fee = fee,
            discountEnabled = discountEnabled,
            offline = offline,
            place = place,
            category = category,
            subCategory = subCategory,
            imageUrl = imageUrl,
            title = title,
            introduction = introduction,
            startDateTime = startDateTime,
            duration = duration
        )
        return gatheringRepository.save(entity)
    }

    private fun validate(
        minAge: Int,
        maxAge: Int,
        maxMaleCount: Int?,
        maxFemaleCount: Int?,
        fee: Int
    ) {
        if (minAge < 1) {
            throw InvalidValueException(
                valueName = "minAge",
                value = minAge,
                reason = null
            )
        }

        if (maxAge < minAge) {
            throw InvalidValueException(
                valueName = "maxAge",
                value = maxAge,
                reason = "maxAge must be greater than or equal to minAge"
            )
        }

        if (maxMaleCount != null && maxMaleCount < 0) {
            throw InvalidValueException(
                valueName = "maxMaleCount",
                value = maxMaleCount,
                reason = "maxMaleCount cannot be negative"
            )
        }

        if (maxFemaleCount != null && maxFemaleCount < 0) {
            throw InvalidValueException(
                valueName = "maxFemaleCount",
                value = maxFemaleCount,
                reason = "maxFemaleCount cannot be negative"
            )
        }

        if (fee % 1000 != 0) {
            throw InvalidValueException("fee", fee, "must be a multiple of 1000")
        }
    }

    fun close(uuid: UUID) {
        val gathering = gatheringRepository.findByUuid(uuid)
            ?: throw IllegalArgumentException("Gathering not found with uuid: $uuid")
        gathering.close()
        gatheringRepository.save(gathering)
    }

    fun join(gatheringUuid: UUID, userUuid: UUID) {
        val token = lockService.lockWithRetry(
            resourceName = "gathering",
            resourceId = gatheringUuid.toString(),
            expire = Duration.ofSeconds(10),
            waitTimeout = Duration.ofSeconds(5),
            retryInterval = Duration.ofMillis(100)
        ) ?: throw IllegalStateException("Failed to acquire lock for gathering: $gatheringUuid")

        try {
            val gathering = gatheringRepository.findByUuid(gatheringUuid)
                ?: throw IllegalArgumentException("Gathering not found with uuid: $gatheringUuid")

            val currentCount = participantRepository.countByGatheringUuid(gatheringUuid)

            if (currentCount + 1 > gathering.maxCapacity) {
                throw IllegalStateException("Gathering is full. Current + 1: ${currentCount + 1} , Max: ${gathering.maxCapacity}")
            }

            participantService.join(
                gatheringUuid = gatheringUuid,
                userUuid = userUuid,
                isHost = false
            )
        } finally {
            lockService.unlock("gathering", gatheringUuid.toString(), token)
        }
    }

    fun schedule(
        hostUuid: UUID,
        approveType: GatheringEntity.ApproveType,
        minCapacity: Int,
        maxCapacity: Int,
        genderRatioEnabled: Boolean,
        minAge: Int,
        maxAge: Int,
        fee: Int,
        discountEnabled: Boolean,
        offline: Boolean,
        place: String,
        category: GatheringEntity.Category,
        subCategory: GatheringEntity.SubCategory,
        imageUrl: String,
        title: String,
        introduction: String,
        scheduleType: ScheduleType,
        weeklySchedule: WeeklySchedule?,
        dateSchedule: DateSchedule?,
        maxMaleCount: Int?,
        maxFemaleCount: Int?
    ) {
        userRepository.findByUuid(hostUuid)?: throw ResourceNotFoundException(
            resourceName = "User",
            resourceId = hostUuid
        )

        val series = seriesRepository.save(
            SeriesEntity(
                uuid = UUID.randomUUID(),
                hostUuid = hostUuid,
                approveType = approveType,
                minCapacity = minCapacity,
                maxCapacity = maxCapacity,
                genderRatioEnabled = genderRatioEnabled,
                minAge = minAge,
                maxAge = maxAge,
                maxMaleCount = maxMaleCount,
                maxFemaleCount = maxFemaleCount,
                fee = fee,
                discountEnabled = discountEnabled,
                offline = offline,
                place = place,
                category = category,
                subCategory = subCategory,
                imageUrl = imageUrl,
                title = title,
                introduction = introduction,
            )
        )

        when (scheduleType) {
            ScheduleType.WEEKLY -> {
                if (weeklySchedule == null) {
                    throw InvalidValueException(
                        valueName = "weeklySchedule",
                        value = "null",
                        reason = "weeklySchedule must be provided for WEEKLY scheduleType"
                    )
                }
                if (weeklySchedule.summaries.isEmpty()) {
                    throw InvalidValueException(
                        valueName = "weeklySchedule.summaries",
                        value = "empty",
                        reason = "weeklySchedule.summaries must not be empty for WEEKLY scheduleType"
                    )
                }
                if (dateSchedule != null) {
                    throw InvalidValueException(
                        valueName = "dateSchedule",
                        value = "not null",
                        reason = "dateSchedule must be null for WEEKLY scheduleType"
                    )
                }

                for(summary in weeklySchedule.summaries) {
                    if (!summary.startTime.isThirtyMinuteInterval()) {
                        throw InvalidValueException(
                            valueName = "summary.startTime",
                            value = summary.startTime,
                            reason = "startTime must be in 30-minute intervals"
                        )
                    }
                    seriesScheduleRepository.save(
                        SeriesScheduleEntity(
                            scheduleType = scheduleType,
                            dayOfWeek = summary.dayOfWeek,
                            scheduleStartDate = weeklySchedule.startDate,
                            scheduleEndDate = weeklySchedule.endDate,
                            date = null,
                            time = summary.startTime,
                            duration = summary.duration,
                            series = series
                        )
                    )
                }
            }
            ScheduleType.DATE -> {
                if (dateSchedule == null) {
                    throw InvalidValueException(
                        valueName = "dateSchedule",
                        value = "null",
                        reason = "dateSchedule must be provided for DATE scheduleType"
                    )
                }
                if (dateSchedule.summaries.isEmpty()) {
                    throw InvalidValueException(
                        valueName = "dateSchedule.summaries",
                        value = "empty",
                        reason = "dateSchedule.summaries must not be empty for DATE scheduleType"
                    )
                }
                if (weeklySchedule != null) {
                    throw InvalidValueException(
                        valueName = "weeklySchedule",
                        value = "not null",
                        reason = "weeklySchedule must be null for DATE scheduleType"
                    )
                }

                for(summary in dateSchedule.summaries) {
                    if (!summary.startTime.isThirtyMinuteInterval()) {
                        throw InvalidValueException(
                            valueName = "summary.startTime",
                            value = summary.startTime,
                            reason = "startTime must be in 30-minute intervals"
                        )
                    }
                    seriesScheduleRepository.save(
                        SeriesScheduleEntity(
                            scheduleType = scheduleType,
                            dayOfWeek = null,
                            scheduleStartDate = null,
                            scheduleEndDate = null,
                            date = summary.date,
                            time = summary.startTime,
                            duration = summary.duration,
                            series = series
                        )
                    )
                }

            }
        }




    }
}