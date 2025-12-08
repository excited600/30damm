package beyondeyesight.domain.service.gathering

import beyondeyesight.domain.exception.ResourceNotFoundException
import beyondeyesight.domain.model.GuestEntity
import beyondeyesight.domain.repository.GuestRepository
import beyondeyesight.domain.repository.UserRepository
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
        val gatheringEntity = gatheringRepository.findByUuid(gatheringUuid) ?: throw ResourceNotFoundException(
            resourceName = "Gathering",
            resourceId = gatheringUuid
        )
        userRepository.findByUuid(userUuid) ?: throw ResourceNotFoundException(
            resourceName = "User",
            resourceId = userUuid
        )
        val guest = GuestEntity.Companion.join(
            gatheringUuid = gatheringEntity.uuid,
            userUuid = userUuid,
        )
        return guestRepository.save(guest)
    }
}