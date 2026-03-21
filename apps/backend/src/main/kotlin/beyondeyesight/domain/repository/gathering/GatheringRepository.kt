package beyondeyesight.domain.repository.gathering

import beyondeyesight.domain.model.ScrollResult
import beyondeyesight.domain.model.gathering.GatheringCursor
import beyondeyesight.domain.model.gathering.GatheringEntity
import beyondeyesight.domain.model.gathering.GatheringFilter
import java.util.UUID

interface GatheringRepository {

    fun findByUuid(uuid: UUID): GatheringEntity?

    fun delete(gatheringEntity: GatheringEntity)

    fun delete(uuid: UUID)

    fun save(gatheringEntity: GatheringEntity): GatheringEntity

    fun scroll(
        cursor: GatheringCursor?,
        size: Int,
        filter: GatheringFilter,
        blockedGatheringUuids: List<UUID>,
        blockedUserUuids: List<UUID>,
    ): ScrollResult<GatheringEntity, GatheringCursor>
}