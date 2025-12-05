package beyondeyesight.ui

import beyondeyesight.application.GatheringApplicationService
import beyondeyesight.domain.model.GatheringEntity
import beyondeyesight.api.GatheringsApiService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
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

    override fun openGathering(openGatheringRequest: beyondeyesight.model.OpenGatheringRequest): beyondeyesight.model.OpenGatheringResponse {
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
            mapper = { gatheringEntity ->
                beyondeyesight.model.OpenGatheringResponse(
                    uuid = gatheringEntity.uuid,
                    minCapacity = gatheringEntity.minCapacity,
                    maxCapacity = gatheringEntity.maxCapacity,
                    genderRatioEnabled = gatheringEntity.genderRatioEnabled,
                    minAge = gatheringEntity.minAge,
                    maxAge = gatheringEntity.maxAge,
                    fee = gatheringEntity.fee,
                    discountEnabled = gatheringEntity.discountEnabled,
                    offline = gatheringEntity.offline,
                    place = gatheringEntity.place,
                    category = beyondeyesight.model.OpenGatheringResponse.Category.valueOf(
                        gatheringEntity.category.name
                    ),
                    subCategory = beyondeyesight.model.OpenGatheringResponse.SubCategory.valueOf(
                        gatheringEntity.subCategory.name
                    ),
                    imageUrl = gatheringEntity.imageUrl,
                    status = beyondeyesight.model.OpenGatheringResponse.Status.valueOf(gatheringEntity.status.name),
                    introduction = gatheringEntity.introduction,
                    approveType = beyondeyesight.model.OpenGatheringResponse.ApproveType.valueOf(
                        gatheringEntity.approveType.name
                    ),
                    startDateTime = gatheringEntity.startDateTime,
                    clickCount = gatheringEntity.clickCount,
                    title = gatheringEntity.title,
                )
            }
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
        val totalAttendees: Int,
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
                    totalAttendees = entity.totalAttendees,
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

