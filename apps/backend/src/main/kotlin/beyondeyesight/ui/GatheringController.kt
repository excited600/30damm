package beyondeyesight.ui

import beyondeyesight.application.GatheringApplicationService
import beyondeyesight.domain.model.gathering.GatheringEntity
import beyondeyesight.api.GatheringsApiService
import beyondeyesight.config.toDurationHours
import beyondeyesight.config.toHoursFloat
import beyondeyesight.domain.exception.InvalidValueException
import beyondeyesight.domain.model.gathering.DateSchedule
import beyondeyesight.domain.model.gathering.ScheduleType
import beyondeyesight.domain.model.gathering.WeeklySchedule
import beyondeyesight.model.DateScheduleSeriesRequest
import beyondeyesight.model.GatheringApproveType
import beyondeyesight.model.GatheringCategory
import beyondeyesight.model.GatheringStatus
import beyondeyesight.model.GatheringSubCategory
import beyondeyesight.model.OpenGatheringRequest
import beyondeyesight.model.OpenGatheringResponse
import beyondeyesight.model.ScheduleSeriesRequest
import beyondeyesight.model.WeeklyScheduleSeriesRequest
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.util.*

@Service
class GatheringController(
    private val gatheringApplicationService: GatheringApplicationService
) : GatheringsApiService {

    fun close(@PathVariable uuid: UUID): ResponseEntity<Unit> {
        gatheringApplicationService.close(uuid)
        return ResponseEntity.noContent().build()
    }

    fun join(
        @PathVariable gatheringUuid: UUID,
        @RequestBody request: JoinGatheringRequest
    ): ResponseEntity<Unit> {
        gatheringApplicationService.join(gatheringUuid, request.userUuid)
        return ResponseEntity.noContent().build()
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
            category = GatheringEntity.Category.valueOf(openGatheringRequest.category.name),
            subCategory = GatheringEntity.SubCategory.valueOf(openGatheringRequest.subCategory.name),
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
            approveType = GatheringEntity.ApproveType.entries.find { it.name == scheduleSeriesRequest.approveType.name } ?: throw InvalidValueException(
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
            category = GatheringEntity.Category.entries.find { it.name == scheduleSeriesRequest.category.name } ?: throw InvalidValueException(
                valueName = "category",
                value = scheduleSeriesRequest.category,
                reason = null
            ),
            subCategory = GatheringEntity.SubCategory.entries.find { it.name == scheduleSeriesRequest.subCategory.name } ?: throw InvalidValueException(
                valueName = "subCategory",
                value = scheduleSeriesRequest.subCategory,
                reason = null
            ),
            imageUrl = scheduleSeriesRequest.imageUrl,
            title = scheduleSeriesRequest.title,
            introduction = scheduleSeriesRequest.introduction,
            scheduleType = ScheduleType.entries.find { it.name == scheduleSeriesRequest.scheduleType.name } ?: throw InvalidValueException(
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

    class JoinGatheringRequest(
        val userUuid: UUID
    )

    data class GatheringDto(
        val uuid: UUID,
        val title: String,
        val introduction: String,
        val category: GatheringEntity.Category,
        val subCategory: String,
        val place: String,
        val fee: Int,
        val maxCapacity: Int,
        val totalGuests: Int,
        val status: GatheringEntity.Status,
        val startDateTime: LocalDateTime,
        val imageUrl: String,
        val createdAt: LocalDateTime
    ) {
        companion object {
            fun from(entity: GatheringEntity): GatheringDto {
                return GatheringDto(
                    uuid = entity.uuid,
                    title = entity.title,
                    introduction = entity.introduction,
                    category = entity.category,
                    subCategory = entity.subCategory.name,
                    place = entity.place,
                    fee = entity.fee,
                    maxCapacity = entity.maxCapacity,
                    totalGuests = entity.totalGuests,
                    status = entity.status,
                    startDateTime = entity.startDateTime,
                    imageUrl = entity.imageUrl,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    data class GetGatheringsResponse(
        val gatherings: List<GatheringDto>,
        val hasNext: Boolean,
        val nextCursor: LocalDateTime?
    ) {
        companion object {
            fun from(
                entities: List<GatheringEntity>,
                hasNext: Boolean,
                nextCursor: LocalDateTime?
            ): GetGatheringsResponse {
                return GetGatheringsResponse(
                    gatherings = entities.map { GatheringDto.from(it) },
                    hasNext = hasNext,
                    nextCursor = nextCursor
                )
            }
        }
    }
}

