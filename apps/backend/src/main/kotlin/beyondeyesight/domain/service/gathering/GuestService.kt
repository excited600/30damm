package beyondeyesight.domain.service.gathering

import beyondeyesight.domain.exception.ResourceNotFoundException
import beyondeyesight.domain.model.GuestEntity
import beyondeyesight.domain.repository.gathering.GuestRepository
import beyondeyesight.domain.repository.user.UserRepository
import beyondeyesight.domain.repository.gathering.GatheringRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GuestService(
    private val guestRepository: GuestRepository,
    private val gatheringRepository: GatheringRepository,
    private val userRepository: UserRepository,
) {
    fun join(gatheringUuid: UUID, userUuid: UUID): GuestEntity {
        val gatheringEntity = gatheringRepository.findByUuid(gatheringUuid) ?: throw ResourceNotFoundException.byUuid(
            resourceName = "Gathering",
            resourceUuid = gatheringUuid
        )
        userRepository.findByUuid(userUuid) ?: throw ResourceNotFoundException.byUuid(
            resourceName = "User",
            resourceUuid = userUuid
        )
        val guest = GuestEntity.join(
            gatheringUuid = gatheringEntity.uuid,
            userUuid = userUuid,
        )
        return guestRepository.save(guest)
    }

    fun leave(gatheringUuid: UUID, userUuid: UUID) {
        guestRepository.deleteByUserUuidAndGatheringUuid(
            userUuid = userUuid,
            gatheringUuid = gatheringUuid
        )
    }
}