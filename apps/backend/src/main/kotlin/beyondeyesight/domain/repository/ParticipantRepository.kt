package beyondeyesight.domain.repository

import beyondeyesight.domain.model.ParticipantEntity
import java.util.UUID

interface ParticipantRepository {
    fun save(participantEntity: ParticipantEntity): ParticipantEntity

    fun countByGatheringUuid(gatheringUuid: UUID): Long
}
