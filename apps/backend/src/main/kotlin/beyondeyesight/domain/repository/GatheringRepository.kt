package beyondeyesight.domain.repository

import beyondeyesight.domain.model.GatheringEntity
import java.time.LocalDateTime
import java.util.UUID

interface GatheringRepository {
    fun create(gatheringEntity: GatheringEntity): GatheringEntity

    fun findByUuid(uuid: UUID): GatheringEntity?

    fun delete(gatheringEntity: GatheringEntity)

    fun delete(uuid: UUID)

    fun save(gatheringEntity: GatheringEntity): GatheringEntity
}