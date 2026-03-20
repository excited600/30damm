package beyondeyesight.domain.repository.gathering.block

import beyondeyesight.domain.model.gathering.block.UserBlockedGatheringEntity
import java.util.UUID

interface UserBlockedGatheringRepository {
    fun save(entity: UserBlockedGatheringEntity): UserBlockedGatheringEntity
    fun findBlockedGatheringUuids(userUuid: UUID): List<UUID>
}
