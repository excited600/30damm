package beyondeyesight.application

import beyondeyesight.domain.model.GatheringEntity
import beyondeyesight.domain.service.GatheringService
import beyondeyesight.domain.service.ParticipantService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

@Service
class GatheringApplicationService(
    private val gatheringService: GatheringService,
    private val participantService: ParticipantService,
) {
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
        category: GatheringEntity.Category,
        subCategory: GatheringEntity.SubCategory,
        imageUrl: String,
        title: String,
        introduction: String,
        startDateTime: LocalDateTime,
        duration: Duration?,
        mapper: (GatheringEntity) -> R
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

        participantService.join(
            gatheringUuid = gatheringEntity.uuid,
            userUuid = hostUuid,
            isHost = true
        )

        return mapper.invoke(gatheringEntity)
    }

    @Transactional
    fun close(uuid: UUID) {
        gatheringService.close(uuid)
    }

    @Transactional
    fun join(gatheringUuid: UUID, userUuid: UUID) {
        gatheringService.join(gatheringUuid, userUuid)
    }
}