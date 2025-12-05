package beyondeyesight.application

import beyondeyesight.domain.model.GatheringEntity
import beyondeyesight.domain.service.GatheringService
import beyondeyesight.domain.service.ParticipantService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
        acceptType: GatheringEntity.AcceptType,
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
        mapper: (GatheringEntity) -> R
    ): R {
        val gatheringEntity = gatheringService.open(
            hostUuid,
            acceptType,
            minCapacity,
            maxCapacity,
            genderRatioEnabled,
            minAge,
            maxAge,
            maxMaleCount,
            maxFemaleCount,
            fee,
            discountEnabled,
            offline,
            place,
            category,
            subCategory,
            imageUrl,
            title,
            introduction,
            startDateTime,
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