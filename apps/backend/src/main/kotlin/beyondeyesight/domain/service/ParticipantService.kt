package beyondeyesight.domain.service

import beyondeyesight.domain.model.ParticipantEntity
import beyondeyesight.domain.repository.ParticipantRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ParticipantService(
    private val participantRepository: ParticipantRepository
) {
    fun join(gatheringUuid: UUID, userUuid: UUID, isHost: Boolean): ParticipantEntity {
        val participant = ParticipantEntity.join(
            gatheringUuid = gatheringUuid,
            userUuid = userUuid,
            isHost = isHost
        )
        return participantRepository.save(participant)
    }
}
