package beyondeyesight.domain.repository.gathering

import beyondeyesight.domain.model.ScrollResult
import beyondeyesight.domain.model.gathering.GatheringEntity
import beyondeyesight.domain.model.gathering.GatheringFilter
import java.util.UUID

interface GatheringRepository {

    fun findByUuid(uuid: UUID): GatheringEntity?

    fun delete(gatheringEntity: GatheringEntity)

    fun delete(uuid: UUID)

    fun save(gatheringEntity: GatheringEntity): GatheringEntity

    fun scroll(
        cursor: UUID?,
        size: Int,
        filter: GatheringFilter,
    ): ScrollResult<GatheringEntity>
}