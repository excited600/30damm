package beyondeyesight.infra.repository

import beyondeyesight.domain.model.ParticipantEntity
import beyondeyesight.domain.repository.ParticipantRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class ParticipantJpaRepositoryAdapter(
    private val participantJpaRepository: ParticipantJpaRepository
) : ParticipantRepository {
    override fun save(participantEntity: ParticipantEntity): ParticipantEntity {
        return participantJpaRepository.save(participantEntity)
    }

    override fun countByGatheringUuid(gatheringUuid: UUID): Long {
        return participantJpaRepository.countByGatheringUuid(gatheringUuid)
    }
}
