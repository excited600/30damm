package beyondeyesight.infra.repository

import beyondeyesight.domain.model.ParticipantEntity
import beyondeyesight.domain.model.ParticipantId
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ParticipantJpaRepository : JpaRepository<ParticipantEntity, ParticipantId> {
    fun countByGatheringUuid(gatheringUuid: UUID): Long
}
