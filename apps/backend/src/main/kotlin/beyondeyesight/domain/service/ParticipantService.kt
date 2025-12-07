package beyondeyesight.domain.service

import beyondeyesight.domain.exception.ResourceNotFoundException
import beyondeyesight.domain.model.ParticipantEntity
import beyondeyesight.domain.repository.GatheringRepository
import beyondeyesight.domain.repository.ParticipantRepository
import beyondeyesight.domain.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ParticipantService(
    private val participantRepository: ParticipantRepository,
    private val gatheringRepository: GatheringRepository,
    private val userRepository: UserRepository,
) {
    fun join(gatheringUuid: UUID, userUuid: UUID, isHost: Boolean): ParticipantEntity {
        gatheringRepository.findByUuid(gatheringUuid)?: throw ResourceNotFoundException(
            resourceName = "Gathering",
            resourceId = gatheringUuid
        )
        userRepository.findByUuid(userUuid) ?: throw ResourceNotFoundException(
            resourceName = "User",
            resourceId = userUuid
        )
        val participant = ParticipantEntity.join(
            gatheringUuid = gatheringUuid,
            userUuid = userUuid,
            isHost = isHost
        )
        return participantRepository.save(participant)
    }
}
