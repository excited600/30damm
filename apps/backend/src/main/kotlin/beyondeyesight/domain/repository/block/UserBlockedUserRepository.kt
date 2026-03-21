package beyondeyesight.domain.repository.block

import beyondeyesight.domain.model.block.UserBlockedUserEntity
import java.util.UUID

interface UserBlockedUserRepository {
    fun save(entity: UserBlockedUserEntity): UserBlockedUserEntity
    fun findBlockedUserUuids(blockerUuid: UUID): List<UUID>
    fun existsByBlockerUuidAndBlockedUuid(blockerUuid: UUID, blockedUuid: UUID): Boolean
}
