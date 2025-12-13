package beyondeyesight.domain.service.gathering

import beyondeyesight.config.isThirtyMinuteInterval
import beyondeyesight.config.uuidV7
import beyondeyesight.domain.exception.DataIntegrityException
import beyondeyesight.domain.exception.InvalidValueException
import beyondeyesight.domain.exception.ResourceNotFoundException
import beyondeyesight.domain.exception.gathering.CannotJoinException
import beyondeyesight.domain.model.user.Gender
import beyondeyesight.domain.model.gathering.*
import beyondeyesight.domain.repository.gathering.GuestRepository
import beyondeyesight.domain.repository.user.UserRepository
import beyondeyesight.domain.repository.gathering.GatheringRepository
import beyondeyesight.domain.repository.gathering.SeriesRepository
import beyondeyesight.domain.repository.gathering.SeriesScheduleRepository
import beyondeyesight.domain.service.LockService
import beyondeyesight.domain.service.payment.PaymentGateway
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@Service
class GatheringService(
    private val gatheringRepository: GatheringRepository,
    private val guestService: GuestService,
    private val guestRepository: GuestRepository,
    private val lockService: LockService,
    private val userRepository: UserRepository,
    private val seriesRepository: SeriesRepository,
    private val seriesScheduleRepository: SeriesScheduleRepository,
    private val paymentGateway: PaymentGateway,
) {
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
        category: Category,
        subCategory: SubCategory,
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

        val entity = GatheringEntity.open(
            hostUuid = host.uuid,
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
            startDateTime = startDateTime,
            score = 0,
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
        userRepository.findByUuid(userUuid) ?: throw ResourceNotFoundException(
            resourceName = "User",
            resourceId = userUuid
        )
        val token = lockService.lockWithRetry(
            resourceName = "gathering",
            resourceId = gatheringUuid.toString(),
            expire = Duration.ofSeconds(10),
            waitTimeout = Duration.ofSeconds(5),
            retryInterval = Duration.ofMillis(100)
        ) ?: throw IllegalStateException("Failed to acquire lock for gathering: $gatheringUuid")

        try {
            val gathering = gatheringRepository.findByUuid(gatheringUuid)
                ?: throw ResourceNotFoundException(
                    resourceName = "Gathering",
                    resourceId = gatheringUuid
                )

            val currentGuestCount = guestRepository.countByGathering(gathering.uuid)

            if (currentGuestCount + 1 > gathering.maxCapacity) {
                throw CannotJoinException.full(
                    userUuid = userUuid,
                    gatheringUuid = gathering.uuid,
                )
            }

            if (gathering.genderRatioEnabled) {
                val currentMaleGuestCount = guestRepository.countByGatheringAndGender(gathering.uuid, Gender.M)
                val maxMaleCount = gathering.maxMaleCount ?: throw DataIntegrityException(
                    tableName = "gathering",
                    resourceUuid = gathering.uuid,
                    cause = "maxMaleCount must not be null when genderRatioEnabled is true"
                )

                if (currentMaleGuestCount + 1 > maxMaleCount) {
                    throw CannotJoinException.full(
                        userUuid = userUuid,
                        gatheringUuid = gathering.uuid,
                        gender = Gender.M
                    )
                }

                val currentFemaleGuestCount = guestRepository.countByGatheringAndGender(
                    gatheringUuid = gathering.uuid,
                    gender = Gender.F
                )
                val maxFemaleCount = gathering.maxFemaleCount ?: throw DataIntegrityException(
                    tableName = "gathering",
                    resourceUuid = gathering.uuid,
                    cause = "maxFemaleCount must not be null when genderRatioEnabled is true"
                )

                if (currentFemaleGuestCount + 1 > maxFemaleCount) {
                    throw CannotJoinException.full(
                        userUuid = userUuid,
                        gatheringUuid = gathering.uuid,
                        gender = Gender.F
                    )
                }
            }

            guestService.join(
                gatheringUuid = gatheringUuid,
                userUuid = userUuid,
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
        category: Category,
        subCategory: SubCategory,
        imageUrl: String,
        title: String,
        introduction: String,
        scheduleType: ScheduleType,
        weeklySchedule: WeeklySchedule?,
        dateSchedule: DateSchedule?,
        gatheringDays: Int,
        maxMaleCount: Int?,
        maxFemaleCount: Int?
    ) {
        userRepository.findByUuid(hostUuid) ?: throw ResourceNotFoundException(
            resourceName = "User",
            resourceId = hostUuid
        )

        val series = seriesRepository.save(
            SeriesEntity(
                uuid = uuidV7(),
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

                for (summary in weeklySchedule.summaries) {
                    if (!summary.startTime.isThirtyMinuteInterval()) {
                        throw InvalidValueException(
                            valueName = "summary.startTime",
                            value = summary.startTime,
                            reason = "startTime must be in 30-minute intervals"
                        )
                    }
                    val openDayOfWeek = summary.startDayOfWeek.minus(gatheringDays.toLong())
                    seriesScheduleRepository.save(
                        SeriesScheduleEntity(
                            scheduleType = scheduleType,
                            openDayOfWeek = openDayOfWeek,
                            startDayOfWeek = summary.startDayOfWeek,
                            scheduleStartDate = weeklySchedule.startDate,
                            scheduleEndDate = weeklySchedule.endDate,
                            openDate = null,
                            startDate = null,
                            startTime = summary.startTime,
                            duration = summary.duration,
                            seriesEntity = series
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

                for (summary in dateSchedule.summaries) {
                    if (!summary.startTime.isThirtyMinuteInterval()) {
                        throw InvalidValueException(
                            valueName = "summary.startTime",
                            value = summary.startTime,
                            reason = "startTime must be in 30-minute intervals"
                        )
                    }

                    val openDate = summary.startDate.minusDays(gatheringDays.toLong())
                    seriesScheduleRepository.save(
                        SeriesScheduleEntity(
                            scheduleType = scheduleType,
                            openDayOfWeek = null,
                            startDayOfWeek = null,
                            scheduleStartDate = null,
                            scheduleEndDate = null,
                            openDate = openDate,
                            startDate = summary.startDate,
                            startTime = summary.startTime,
                            duration = summary.duration,
                            seriesEntity = series
                        )
                    )
                }

            }
        }


    }
}