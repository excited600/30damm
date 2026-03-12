package beyondeyesight.application

import beyondeyesight.domain.model.gathering.Category
import beyondeyesight.domain.model.gathering.DateSchedule
import beyondeyesight.domain.model.gathering.GatheringCursor
import beyondeyesight.domain.model.gathering.GatheringEntity
import beyondeyesight.domain.model.gathering.GatheringFilter
import beyondeyesight.domain.model.gathering.ScheduleType
import beyondeyesight.domain.model.gathering.WeeklySchedule
import beyondeyesight.domain.model.payment.ConfirmPaymentRequest
import beyondeyesight.domain.model.gathering.GatheringUserStatus
import beyondeyesight.domain.service.gathering.GatheringService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

@Service
class GatheringApplicationService(
    private val gatheringService: GatheringService,
) {
    @Transactional
    fun <R> open(
        hostUuid: UUID,
        title: String,
        description: String?,
        category: Category,
        location: String?,
        startDateTime: LocalDateTime?,
        duration: Duration?,
        minCapacity: Int,
        maxCapacity: Int,
        genderRatioEnabled: Boolean,
        maxMaleCount: Int?,
        maxFemaleCount: Int?,
        isFree: Boolean,
        price: Int?,
        isSplit: Boolean,
        imageUrl: String?,
        mapper: (GatheringEntity) -> R
    ): R {
        val gatheringEntity = gatheringService.open(
            hostUuid = hostUuid,
            title = title,
            description = description,
            category = category,
            location = location,
            startDateTime = startDateTime,
            duration = duration,
            minCapacity = minCapacity,
            maxCapacity = maxCapacity,
            genderRatioEnabled = genderRatioEnabled,
            maxMaleCount = maxMaleCount,
            maxFemaleCount = maxFemaleCount,
            isFree = isFree,
            price = price,
            isSplit = isSplit,
            imageUrl = imageUrl,
        )
        return mapper.invoke(gatheringEntity)
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

    @Transactional(readOnly = true)
    fun <R> scroll(
        cursor: GatheringCursor?,
        size: Int,
        filter: GatheringFilter,
        mapper: (GatheringService.ScrollWithDetails) -> R
    ): R {
        val details = gatheringService.scroll(
            cursor = cursor,
            size = size,
            filter = filter,
        )
        return mapper.invoke(details)
    }

    @Transactional(readOnly = true)
    fun <R> getDetail(
        userUuid: UUID,
        gatheringUuid: UUID,
        mapper: (GatheringService.GatheringDetail) -> R
    ): R {
        val detail = gatheringService.getDetail(userUuid, gatheringUuid)
        return mapper.invoke(detail)
    }

    @Transactional
    fun schedule(
        hostUuid: UUID,
        minCapacity: Int,
        maxCapacity: Int,
        genderRatioEnabled: Boolean,
        fee: Int,
        isSplit: Boolean,
        place: String?,
        category: Category,
        imageUrl: String?,
        title: String,
        description: String?,
        scheduleType: ScheduleType,
        weeklySchedule: WeeklySchedule?,
        dateSchedule: DateSchedule?,
        gatheringDays: Int,
        maxMaleCount: Int?,
        maxFemaleCount: Int?,
    ) {
        gatheringService.schedule(
            hostUuid = hostUuid,
            minCapacity = minCapacity,
            maxCapacity = maxCapacity,
            genderRatioEnabled = genderRatioEnabled,
            fee = fee,
            isSplit = isSplit,
            place = place,
            category = category,
            imageUrl = imageUrl,
            title = title,
            description = description,
            scheduleType = scheduleType,
            weeklySchedule = weeklySchedule,
            dateSchedule = dateSchedule,
            gatheringDays = gatheringDays,
            maxMaleCount = maxMaleCount,
            maxFemaleCount = maxFemaleCount,
        )
    }
}
