package beyondeyesight.application

import beyondeyesight.domain.exception.InvalidValueException
import beyondeyesight.domain.model.gathering.Category
import beyondeyesight.domain.model.gathering.DateSchedule
import beyondeyesight.domain.model.gathering.GatheringEntity
import beyondeyesight.domain.model.gathering.ScheduleType
import beyondeyesight.domain.model.gathering.SubCategory
import beyondeyesight.domain.model.gathering.WeeklySchedule
import beyondeyesight.domain.model.payment.ConfirmPaymentRequest
import beyondeyesight.domain.service.gathering.GatheringService
import beyondeyesight.domain.service.gathering.GuestService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

@Service
class GatheringApplicationService(
    private val gatheringService: GatheringService,
) {
    @Transactional
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
        maxFemaleCount: Int?,
    ) {

        gatheringService.schedule(
            hostUuid = hostUuid,
            approveType = GatheringEntity.ApproveType.entries.find { it.name == approveType.name }
                ?: throw InvalidValueException(
                    valueName = "approveType",
                    value = approveType,
                    reason = null
                ),
            minCapacity = minCapacity,
            maxCapacity = maxCapacity,
            genderRatioEnabled = genderRatioEnabled,
            minAge = minAge,
            maxAge = maxAge,
            fee = fee,
            discountEnabled = discountEnabled,
            offline = offline,
            place = place,
            category = Category.entries.find { it.name == category.name }
                ?: throw InvalidValueException(
                    valueName = "category",
                    value = category,
                    reason = null
                ),
            subCategory = SubCategory.entries.find { it.name == subCategory.name }
                ?: throw InvalidValueException(
                    valueName = "subCategory",
                    value = subCategory,
                    reason = null
                ),
            imageUrl = imageUrl,
            title = title,
            introduction = introduction,
            scheduleType = ScheduleType.entries.find { it.name == scheduleType.name } ?: throw InvalidValueException(
                valueName = "scheduleType",
                value = scheduleType,
                reason = null
            ),
            weeklySchedule = weeklySchedule?.let { weeklySchedule ->
                WeeklySchedule(
                    startDate = weeklySchedule.startDate,
                    endDate = weeklySchedule.endDate,
                    summaries = weeklySchedule.summaries.map { summary ->
                        WeeklySchedule.WeeklyScheduleSummary(
                            startDayOfWeek = DayOfWeek.entries.find { it.name == summary.startDayOfWeek.name }
                                ?: throw InvalidValueException(
                                    valueName = "dayOfWeek",
                                    value = summary.startDayOfWeek,
                                    reason = null
                                ),
                            startTime = summary.startTime,
                            duration = summary.duration
                        )
                    }
                )
            },
            dateSchedule = dateSchedule?.let { dateSchedule ->
                DateSchedule(
                    dateSchedule.summaries.map { summary ->
                        DateSchedule.DateScheduleSummary(
                            startDate = summary.startDate,
                            startTime = summary.startTime,
                            duration = summary.duration
                        )
                    }
                )
            },
            gatheringDays = gatheringDays,
            maxMaleCount = maxMaleCount,
            maxFemaleCount = maxFemaleCount

        )
    }

    @Transactional
    fun <R> open(
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
        mapper: (GatheringEntity, UUID) -> R
    ): R {
        val gatheringEntity = gatheringService.open(
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
            startDateTime = startDateTime,
            duration = duration
        )

        return mapper.invoke(gatheringEntity, hostUuid)
    }

    @Transactional
    fun close(uuid: UUID) {
        gatheringService.close(uuid)
    }

    @Transactional
    fun join(gatheringUuid: UUID, userUuid: UUID, confirmPaymentRequest: ConfirmPaymentRequest?) {
        gatheringService.join(
            gatheringUuid = gatheringUuid,
            userUuid = userUuid,
            confirmPaymentRequest = confirmPaymentRequest,
        )
    }

    @Transactional
    fun leave(userUuid: UUID, gatheringUuid: UUID, reason: String) {
        gatheringService.leave(
            gatheringUuid = gatheringUuid,
            userUuid = userUuid,
            reason = reason
        )
    }
}