package beyondeyesight.domain.repository.gathering

import beyondeyesight.domain.model.gathering.GatheringEntity
import java.util.UUID

interface GatheringRepository {

    fun findByUuid(uuid: UUID): GatheringEntity?

    fun delete(gatheringEntity: GatheringEntity)

    fun delete(uuid: UUID)

    fun save(gatheringEntity: GatheringEntity): GatheringEntity
}