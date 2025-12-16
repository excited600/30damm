package beyondeyesight.ui

import beyondeyesight.api.GatheringsApiService
import beyondeyesight.application.GatheringApplicationService
import beyondeyesight.config.toDurationHours
import beyondeyesight.config.toHoursFloat
import beyondeyesight.domain.exception.InvalidValueException
import beyondeyesight.domain.model.ScrollResult
import beyondeyesight.domain.model.gathering.*
import beyondeyesight.domain.model.payment.ConfirmPaymentRequest
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
                ConfirmPaymentRequest(
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
        uuid: UUID?,
        score: Int?,
        statuses: List<GatheringStatus>?,
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
        val cursor = if (uuid != null && score != null) {
            GatheringCursor(
                uuid = uuid,
                score = score
            )
        } else {
            null
        }

        val filter = GatheringFilter(
            statuses = statuses?.map { requestStatus ->
                Status.entries.find { it.name == requestStatus.name } ?: throw InvalidValueException(
                    valueName = "staus",
                    value = requestStatus,
                    reason = "존재하지 않는 모임 상태."
                )
            },
            categories = categories?.map { requestCategory ->
                Category.entries.find { it.name == requestCategory.name } ?: throw InvalidValueException(
                    valueName = "category",
                    value = requestCategory,
                    reason = "존재하지 않는 모임 카테고리."
                )
            },
            guestCount = guestCount,
            dayOfWeek = dayOfWeek?.let { requestDayOfWeek ->
                DayOfWeek.entries.find { it.name == requestDayOfWeek.name } ?: throw InvalidValueException(
                    valueName = "dayOfWeek",
                    value = requestDayOfWeek,
                    reason = "존재하지 않는 요일."
                )
            },
            startDate = startDate,
            endDate = endDate,
            location = location,
            startAge = startAge,
            endAge = endAge,
            genderRatioEnabled = genderRatioEnabled,
            minCapacity = minCapacity,
            maxCapacity = maxCapacity,
            minFee = minFee,
            maxFee = maxFee
        )
        return gatheringApplicationService.scroll(
            cursor = cursor,
            size = size,
            filter = filter,
            mapper = { scrollResult: ScrollResult<GatheringEntity, GatheringCursor> ->
                ScrollFilteredGatheringsResponse(
                    hasNext = scrollResult.hasNext,
                    gatherings = scrollResult.items.map { gatheringEntity ->
                        Gathering(
                            uuid = gatheringEntity.uuid,
                            hostUuid = gatheringEntity.hostUuid,
                            approveType = GatheringApproveType.entries.find { it.name == gatheringEntity.approveType.name }
                                ?: throw InvalidValueException(
                                    valueName = "approveType",
                                    value = gatheringEntity.approveType,
                                    reason = "존재하지 않는 approveType"
                                ),
                            minCapacity = gatheringEntity.minCapacity,
                            maxCapacity = gatheringEntity.maxCapacity,
                            genderRatioEnabled = gatheringEntity.genderRatioEnabled,
                            minAge = gatheringEntity.minAge,
                            maxAge = gatheringEntity.maxAge,
                            totalGuests = gatheringEntity.totalGuests,
                            fee = gatheringEntity.fee,
                            discountEnabled = gatheringEntity.discountEnabled,
                            offline = gatheringEntity.offline,
                            place = gatheringEntity.place,
                            category = GatheringCategory.entries.find { category -> category.name == gatheringEntity.category.name }
                                ?: throw InvalidValueException(
                                    valueName = "category",
                                    value = gatheringEntity.category,
                                    reason = "존재하지 않는 카테고리"
                                ),
                            subCategory = GatheringSubCategory.entries.find { it.name == gatheringEntity.subCategory.name }
                                ?: throw InvalidValueException(
                                    valueName = "subCategory",
                                    value = gatheringEntity.subCategory,
                                    reason = "존재하지 않는 서브카테고리"
                                ),
                            status = GatheringStatus.entries.find { it.name == gatheringEntity.status.name }
                                ?: throw InvalidValueException(
                                    valueName = "status",
                                    value = gatheringEntity.status,
                                    reason = "존재하지 않는 status"
                                ),
                            imageUrl = gatheringEntity.imageUrl,
                            title = gatheringEntity.title,
                            introduction = gatheringEntity.introduction,
                            clickCount = gatheringEntity.clickCount,
                            startDateTime = gatheringEntity.startDateTime,
                            maxMaleCount = gatheringEntity.maxMaleCount,
                            maxFemaleCount = gatheringEntity.maxFemaleCount,
                            duration = gatheringEntity.duration.toHoursFloat()
                        )
                    },
                    cursor = ScrollFilteredGatheringsResponseCursor(
                        score = scrollResult.cursor.score,
                        uuid = scrollResult.cursor.uuid
                    )
                )
            }
        )
    }
}

