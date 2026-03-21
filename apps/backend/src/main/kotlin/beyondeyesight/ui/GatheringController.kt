package beyondeyesight.ui

import beyondeyesight.api.GatheringsApiService
import beyondeyesight.application.GatheringApplicationService
import beyondeyesight.config.currentUserUuid
import beyondeyesight.config.toDurationHours
import beyondeyesight.domain.exception.DataIntegrityException
import beyondeyesight.domain.exception.InvalidValueException
import beyondeyesight.domain.exception.ResourceNotFoundException
import beyondeyesight.domain.model.gathering.*
import beyondeyesight.domain.model.payment.ConfirmPaymentRequest
import beyondeyesight.domain.model.user.UserEntity
import beyondeyesight.model.*
import org.springframework.stereotype.Service
import java.net.URI
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class GatheringController(
    private val gatheringApplicationService: GatheringApplicationService
) : GatheringsApiService {

    override fun openGathering(openGatheringRequest: OpenGatheringRequest): OpenGatheringResponse {
        val startDateTime = if (openGatheringRequest.date != null && openGatheringRequest.startTime != null) {
            val time = LocalTime.parse(openGatheringRequest.startTime, DateTimeFormatter.ofPattern("HH:mm"))
            openGatheringRequest.date.atTime(time)
        } else {
            null
        }

        val duration = openGatheringRequest.duration?.let { Duration.ofMinutes(it.toLong()) }

        return gatheringApplicationService.open(
            hostUuid = currentUserUuid(),
            title = openGatheringRequest.title,
            description = openGatheringRequest.description,
            category = Category.valueOf(openGatheringRequest.category.name),
            location = openGatheringRequest.location,
            startDateTime = startDateTime,
            duration = duration,
            minCapacity = openGatheringRequest.minCapacity,
            maxCapacity = openGatheringRequest.maxCapacity,
            genderRatioEnabled = openGatheringRequest.isGenderRatioEnabled,
            maxMaleCount = openGatheringRequest.maxMaleCapacity,
            maxFemaleCount = openGatheringRequest.maxFemaleCapacity,
            isFree = openGatheringRequest.isFree,
            price = openGatheringRequest.price,
            isSplit = openGatheringRequest.isSplit,
            imageUrl = null,
            mapper = { gatheringEntity ->
                OpenGatheringResponse(gatheringUuid = gatheringEntity.uuid)
            }
        )
    }

    override fun getGatheringDetail(gatheringId: UUID): GetGatheringDetailResponse {
        return gatheringApplicationService.getDetail(
            userUuid = currentUserUuid(),
            gatheringUuid = gatheringId,
            mapper = { detail ->
                val gathering = detail.gathering
                GetGatheringDetailResponse(
                    gatheringUuid = gathering.uuid,
                    title = gathering.title,
                    description = gathering.description ?: "",
                    host = ScrollFilteredGatheringsResponseListInnerHost(
                        userUuid = detail.host.user.uuid,
                        nickname = detail.host.user.nickname,
                        gender = requireGender(detail.host.user),
                        profileImageUrl = detail.host.user.profileImageUrl?.let { URI(it) },
                        viewerRelation = ViewerRelation.forValue(detail.host.viewerRelation.name),
                    ),
                    guests = detail.guests.map { guestView ->
                        GetGatheringDetailResponseGuestsInner(
                            userUuid = guestView.user.uuid,
                            nickname = guestView.user.nickname,
                            gender = requireGender(guestView.user),
                            profileImageUrl = guestView.user.profileImageUrl?.let { URI(it) },
                            viewerRelation = ViewerRelation.forValue(guestView.viewerRelation.name),
                        )
                    },
                    category = GatheringCategory.valueOf(gathering.category.name),
                    minCapacity = gathering.minCapacity,
                    maxCapacity = gathering.maxCapacity,
                    isGenderRatioEnabled = gathering.genderRatioEnabled,
                    maxMaleCapacity = gathering.maxMaleCount,
                    maxFemaleCapacity = gathering.maxFemaleCount,
                    currentMaleCount = detail.genderCounts.maleCount,
                    currentFemaleCount = detail.genderCounts.femaleCount,
                    date = gathering.startDateTime?.toLocalDate(),
                    startTime = gathering.startDateTime?.toLocalTime()?.format(DateTimeFormatter.ofPattern("HH:mm")),
                    duration = gathering.duration?.toMinutes()?.toInt(),
                    location = gathering.place,
                    isFree = gathering.isFree,
                    isSplit = gathering.isSplit,
                    price = gathering.fee,
                    imgUrl = gathering.imageUrl?.let { URI(it) },
                    userStatus = beyondeyesight.model.GatheringUserStatus.forValue(detail.userStatus.name),
                )
            }
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
            GatheringCursor(uuid = uuid, score = score)
        } else {
            null
        }

        val filter = GatheringFilter(
            statuses = statuses?.map { requestStatus ->
                Status.entries.find { it.name == requestStatus.name } ?: throw InvalidValueException(
                    valueName = "status",
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
                java.time.DayOfWeek.entries.find { it.name == requestDayOfWeek.name } ?: throw InvalidValueException(
                    valueName = "dayOfWeek",
                    value = requestDayOfWeek,
                    reason = "존재하지 않는 요일."
                )
            },
            startDate = startDate,
            endDate = endDate,
            location = location,
            genderRatioEnabled = genderRatioEnabled,
            minCapacity = minCapacity,
            maxCapacity = maxCapacity,
            minFee = minFee,
            maxFee = maxFee
        )

        return gatheringApplicationService.scroll(
            userUuid = currentUserUuid(),
            cursor = cursor,
            size = size,
            filter = filter,
            mapper = { details ->
                ScrollFilteredGatheringsResponse(
                    hasNext = details.scrollResult.hasNext,
                    list = details.scrollResult.items.map { gathering ->
                        val hostView = details.hostViews[gathering.hostUuid]
                            ?: throw ResourceNotFoundException.byUuid(
                                resourceName = UserEntity.RESOURCE_NAME,
                                resourceUuid = gathering.hostUuid
                            )
                        val genderCounts = details.genderCountsMap[gathering.uuid]
                            ?: throw DataIntegrityException(
                                tableName = "gatherings",
                                resourceUuid = gathering.uuid,
                                cause = "gender counts not found"
                            )
                        ScrollFilteredGatheringsResponseListInner(
                            gatheringUuid = gathering.uuid,
                            title = gathering.title,
                            maleCount = genderCounts.maleCount,
                            femaleCount = genderCounts.femaleCount,
                            host = ScrollFilteredGatheringsResponseListInnerHost(
                                userUuid = hostView.user.uuid,
                                nickname = hostView.user.nickname,
                                gender = requireGender(hostView.user),
                                profileImageUrl = hostView.user.profileImageUrl?.let { URI(it) },
                                viewerRelation = ViewerRelation.forValue(hostView.viewerRelation.name),
                            ),
                            isFree = gathering.isFree,
                            isSplit = gathering.isSplit,
                            imgUrl = gathering.imageUrl?.let { URI(it) },
                            location = gathering.place,
                            date = gathering.startDateTime?.toLocalDate(),
                            startTime = gathering.startDateTime?.toLocalTime()?.format(DateTimeFormatter.ofPattern("HH:mm")),
                            duration = gathering.duration?.toMinutes()?.toInt(),
                            price = gathering.fee,
                        )
                    },
                    cursor = details.scrollResult.cursor?.let {
                        ScrollFilteredGatheringsResponseCursor(
                            score = it.score,
                            uuid = it.uuid
                        )
                    }
                )
            }
        )
    }

    override fun joinGathering(
        gatheringUuid: UUID,
        joinGatheringRequest: JoinGatheringRequest
    ) {
        gatheringApplicationService.join(
            gatheringUuid = gatheringUuid,
            userUuid = currentUserUuid(),
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

    override fun scheduleSeries(scheduleSeriesRequest: ScheduleSeriesRequest) {
        gatheringApplicationService.schedule(
            hostUuid = currentUserUuid(),
            minCapacity = scheduleSeriesRequest.minCapacity,
            maxCapacity = scheduleSeriesRequest.maxCapacity,
            genderRatioEnabled = scheduleSeriesRequest.genderRatioEnabled,
            fee = scheduleSeriesRequest.fee,
            isSplit = false,
            place = scheduleSeriesRequest.place,
            category = Category.entries.find { it.name == scheduleSeriesRequest.category.name }
                ?: throw InvalidValueException(
                    valueName = "category",
                    value = scheduleSeriesRequest.category,
                    reason = null
                ),
            imageUrl = scheduleSeriesRequest.imageUrl,
            title = scheduleSeriesRequest.title,
            description = scheduleSeriesRequest.description,
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

    private fun requireGender(user: UserEntity): Gender {
        val domainGender = user.gender ?: throw DataIntegrityException(
            tableName = "users",
            resourceUuid = user.uuid,
            cause = "gender must not be null"
        )
        return Gender.valueOf(domainGender.name)
    }
}
