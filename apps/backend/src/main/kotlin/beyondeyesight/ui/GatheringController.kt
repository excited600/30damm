package beyondeyesight.ui

import beyondeyesight.api.GatheringsApiService
import beyondeyesight.application.GatheringApplicationService
import beyondeyesight.config.toDurationHours
import beyondeyesight.config.toHoursFloat
import beyondeyesight.domain.exception.InvalidValueException
import beyondeyesight.domain.model.gathering.*
import beyondeyesight.model.*
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*

@Service
class GatheringController(
    private val gatheringApplicationService: GatheringApplicationService
) : GatheringsApiService {

    fun close(@PathVariable uuid: UUID): ResponseEntity<Unit> {
        // TODO: 환불 로직
        gatheringApplicationService.close(uuid)
        return ResponseEntity.noContent().build()
    }

    override fun joinGathering(
        gatheringUuid: UUID,
        joinGatheringRequest: JoinGatheringRequest
    ) {
        gatheringApplicationService.join(
            gatheringUuid = gatheringUuid,
            userUuid = joinGatheringRequest.userUuid,
            confirmPaymentRequest = joinGatheringRequest.confirmPaymentRequest?.let {
                beyondeyesight.domain.model.payment.ConfirmPaymentRequest(
                    paymentId = it.paymentId,
                    amount = it.amount,
                    paymentToken = it.paymentToken,
                    txId = it.txId,
                )
            }
        )
    }

    override fun leaveGathering(
        gatheringUuid: UUID,
        leaveGatheringRequest: LeaveGatheringRequest
    ) {
        gatheringApplicationService.leave(
            gatheringUuid = gatheringUuid,
            userUuid = leaveGatheringRequest.userUuid,
            reason = leaveGatheringRequest.reason
        )
    }

    override fun openGathering(openGatheringRequest: OpenGatheringRequest): OpenGatheringResponse {
        return gatheringApplicationService.open(
            hostUuid = openGatheringRequest.hostUuid,
            approveType = GatheringEntity.ApproveType.valueOf(openGatheringRequest.approveType.name),
            minCapacity = openGatheringRequest.minCapacity,
            maxCapacity = openGatheringRequest.maxCapacity,
            genderRatioEnabled = openGatheringRequest.genderRatioEnabled,
            minAge = openGatheringRequest.minAge,
            maxAge = openGatheringRequest.maxAge,
            maxMaleCount = openGatheringRequest.maxMaleCount,
            maxFemaleCount = openGatheringRequest.maxFemaleCount,
            fee = openGatheringRequest.fee,
            discountEnabled = openGatheringRequest.discountEnabled,
            offline = openGatheringRequest.offline,
            place = openGatheringRequest.place,
            category = Category.valueOf(openGatheringRequest.category.name),
            subCategory = SubCategory.valueOf(openGatheringRequest.subCategory.name),
            imageUrl = openGatheringRequest.imageUrl,
            title = openGatheringRequest.title,
            introduction = openGatheringRequest.introduction,
            startDateTime = openGatheringRequest.startDateTime,
            duration = openGatheringRequest.duration.toDurationHours(),
            mapper = { gatheringEntity: GatheringEntity, hostUuid: UUID ->
                OpenGatheringResponse(
                    uuid = gatheringEntity.uuid,
                    hostUuid = hostUuid,
                    minCapacity = gatheringEntity.minCapacity,
                    maxCapacity = gatheringEntity.maxCapacity,
                    genderRatioEnabled = gatheringEntity.genderRatioEnabled,
                    minAge = gatheringEntity.minAge,
                    maxAge = gatheringEntity.maxAge,
                    fee = gatheringEntity.fee,
                    discountEnabled = gatheringEntity.discountEnabled,
                    offline = gatheringEntity.offline,
                    place = gatheringEntity.place,
                    category = GatheringCategory.valueOf(
                        gatheringEntity.category.name
                    ),
                    subCategory = GatheringSubCategory.valueOf(
                        gatheringEntity.subCategory.name
                    ),
                    imageUrl = gatheringEntity.imageUrl,
                    status = GatheringStatus.valueOf(gatheringEntity.status.name),
                    introduction = gatheringEntity.introduction,
                    approveType = GatheringApproveType.valueOf(
                        gatheringEntity.approveType.name
                    ),
                    startDateTime = gatheringEntity.startDateTime,
                    duration = gatheringEntity.duration.toHoursFloat(),
                    clickCount = gatheringEntity.clickCount,
                    title = gatheringEntity.title,
                    totalGuests = gatheringEntity.totalGuests
                )
            }
        )
    }

    override fun scheduleSeries(scheduleSeriesRequest: ScheduleSeriesRequest) {
        gatheringApplicationService.schedule(
            hostUuid = scheduleSeriesRequest.hostUuid,
            approveType = GatheringEntity.ApproveType.entries.find { it.name == scheduleSeriesRequest.approveType.name }
                ?: throw InvalidValueException(
                    valueName = "approveType",
                    value = scheduleSeriesRequest.approveType,
                    reason = null
                ),
            minCapacity = scheduleSeriesRequest.minCapacity,
            maxCapacity = scheduleSeriesRequest.maxCapacity,
            genderRatioEnabled = scheduleSeriesRequest.genderRatioEnabled,
            minAge = scheduleSeriesRequest.minAge,
            maxAge = scheduleSeriesRequest.maxAge,
            fee = scheduleSeriesRequest.fee,
            discountEnabled = scheduleSeriesRequest.discountEnabled,
            offline = scheduleSeriesRequest.offline,
            place = scheduleSeriesRequest.place,
            category = Category.entries.find { it.name == scheduleSeriesRequest.category.name }
                ?: throw InvalidValueException(
                    valueName = "category",
                    value = scheduleSeriesRequest.category,
                    reason = null
                ),
            subCategory = SubCategory.entries.find { it.name == scheduleSeriesRequest.subCategory.name }
                ?: throw InvalidValueException(
                    valueName = "subCategory",
                    value = scheduleSeriesRequest.subCategory,
                    reason = null
                ),
            imageUrl = scheduleSeriesRequest.imageUrl,
            title = scheduleSeriesRequest.title,
            introduction = scheduleSeriesRequest.introduction,
            scheduleType = ScheduleType.entries.find { it.name == scheduleSeriesRequest.scheduleType.name }
                ?: throw InvalidValueException(
                    valueName = "scheduleType",
                    value = scheduleSeriesRequest.scheduleType,
                    reason = null
                ),
            weeklySchedule = (scheduleSeriesRequest as? WeeklyScheduleSeriesRequest)?.let { request ->
                WeeklySchedule(
                    startDate = request.startDate,
                    endDate = request.endDate,
                    summaries = request.summaries.map { summary ->
                        WeeklySchedule.WeeklyScheduleSummary(
                            startDayOfWeek = DayOfWeek.entries.find { it.name == summary.startDayOfWeek.name }
                                ?: throw InvalidValueException(
                                    valueName = "dayOfWeek",
                                    value = summary.startDayOfWeek,
                                    reason = null
                                ),
                            startTime = summary.startTime,
                            duration = summary.duration.toDurationHours()
                        )
                    }
                )
            },
            dateSchedule = (scheduleSeriesRequest as? DateScheduleSeriesRequest)?.let { request ->
                DateSchedule(
                    request.summaries.map { summary ->
                        DateSchedule.DateScheduleSummary(
                            startDate = summary.startDate,
                            startTime = summary.startTime,
                            duration = summary.duration.toDurationHours()
                        )
                    }
                )
            },
            gatheringDays = scheduleSeriesRequest.gatheringDays,
            maxMaleCount = scheduleSeriesRequest.maxMaleCount,
            maxFemaleCount = scheduleSeriesRequest.maxFemaleCount
        )
    }

    override fun scrollFilteredGatherings(
        size: Int,
        cursor: String?,
        categories: List<GatheringCategory>?,
        guestCount: Int?,
        dayOfWeek: GatheringDayOfWeek?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        location: String?,
        startAge: Int?,
        endAge: Int?,
        genderRatioEnabled: Boolean?,
        minCapacity: Int?,
        maxCapacity: Int?,
        minFee: Int?,
        maxFee: Int?
    ): ScrollFilteredGatheringsResponse {
        TODO("Not yet implemented")
    }
}

