package beyondeyesight.domain.service

import beyondeyesight.domain.exception.ResourceNotFoundException
import beyondeyesight.domain.model.GuestEntity
import beyondeyesight.domain.repository.gathering.GatheringRepository
import beyondeyesight.domain.repository.GuestRepository
import beyondeyesight.domain.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GuestService(
    private val guestRepository: GuestRepository,
    private val gatheringRepository: GatheringRepository,
    private val userRepository: UserRepository,
) {
    fun join(gatheringUuid: UUID, userUuid: UUID): GuestEntity {
        gatheringRepository.findByUuid(gatheringUuid)?: throw ResourceNotFoundException(
            resourceName = "Gathering",
            resourceId = gatheringUuid
        )
        userRepository.findByUuid(userUuid) ?: throw ResourceNotFoundException(
            resourceName = "User",
            resourceId = userUuid
        )
        val guest = GuestEntity.join(
            gatheringUuid = gatheringUuid,
            userUuid = userUuid,
        )
        return guestRepository.save(guest)
    }
}
