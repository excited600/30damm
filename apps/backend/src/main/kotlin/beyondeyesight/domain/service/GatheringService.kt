package beyondeyesight.domain.service

import beyondeyesight.domain.exception.ResourceNotFoundException
import beyondeyesight.domain.model.GatheringEntity
import beyondeyesight.domain.model.UserEntity
import beyondeyesight.domain.repository.GatheringRepository
import beyondeyesight.domain.repository.ParticipantRepository
import beyondeyesight.domain.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

@Service
class GatheringService(
    private val gatheringRepository: GatheringRepository,
    private val participantService: ParticipantService,
    private val participantRepository: ParticipantRepository,
    private val lockService: LockService,
    private val userRepository: UserRepository,
) {

    fun open(
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
    ): GatheringEntity {
        val host = userRepository.findByUuid(hostUuid) ?: throw ResourceNotFoundException(
            resourceName = "User",
            resourceId = hostUuid
        )

        val (currentMaleCount, currentFemaleCount) =
            if (host.gender == UserEntity.Gender.M) {
               1 to 0
            } else {
                0 to 1
            }
        val entity = GatheringEntity.open(
            acceptType = acceptType,
            minCapacity = minCapacity,
            maxCapacity = maxCapacity,
            genderRatioEnabled = genderRatioEnabled,
            minAge = minAge,
            maxAge = maxAge,
            maxMaleCount = maxMaleCount,
            maxFemaleCount = maxFemaleCount,
            currentMaleCount = currentMaleCount,
            currentFemaleCount = currentFemaleCount,
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
        )
        return gatheringRepository.create(entity)
    }

    fun close(uuid: UUID) {
        val gathering = gatheringRepository.findByUuid(uuid)
            ?: throw IllegalArgumentException("Gathering not found with uuid: $uuid")
        gathering.close()
        gatheringRepository.save(gathering)
    }

    fun join(gatheringUuid: UUID, userUuid: UUID) {
        val token = lockService.lockWithRetry(
            resourceName = "gathering",
            resourceId = gatheringUuid.toString(),
            expire = Duration.ofSeconds(10),
            waitTimeout = Duration.ofSeconds(5),
            retryInterval = Duration.ofMillis(100)
        ) ?: throw IllegalStateException("Failed to acquire lock for gathering: $gatheringUuid")

        try {
            val gathering = gatheringRepository.findByUuid(gatheringUuid)
                ?: throw IllegalArgumentException("Gathering not found with uuid: $gatheringUuid")

            val currentCount = participantRepository.countByGatheringUuid(gatheringUuid)

            if (currentCount + 1 > gathering.maxCapacity) {
                throw IllegalStateException("Gathering is full. Current + 1: ${currentCount + 1} , Max: ${gathering.maxCapacity}")
            }

            participantService.join(
                gatheringUuid = gatheringUuid,
                userUuid = userUuid,
                isHost = false
            )
        } finally {
            lockService.unlock("gathering", gatheringUuid.toString(), token)
        }
    }
}