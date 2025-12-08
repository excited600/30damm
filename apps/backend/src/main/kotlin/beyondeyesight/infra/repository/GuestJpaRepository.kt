package beyondeyesight.infra.repository

import beyondeyesight.domain.model.GuestEntity
import beyondeyesight.domain.model.GuestId
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface GuestJpaRepository : JpaRepository<GuestEntity, GuestId> {
    fun countByGatheringUuid(gatheringUuid: UUID): Long
}
