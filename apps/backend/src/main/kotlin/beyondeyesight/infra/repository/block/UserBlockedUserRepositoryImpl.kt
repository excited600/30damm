package beyondeyesight.infra.repository.block

import beyondeyesight.domain.model.block.UserBlockedUserEntity
import beyondeyesight.domain.repository.block.UserBlockedUserRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserBlockedUserRepositoryImpl(
    private val jpaRepository: UserBlockedUserJpaRepository,
) : UserBlockedUserRepository {

    override fun save(entity: UserBlockedUserEntity): UserBlockedUserEntity {
        return jpaRepository.save(entity)
    }

    override fun findBlockedUserUuids(blockerUuid: UUID): List<UUID> {
        return jpaRepository.findBlockedUuidsByBlockerUuid(blockerUuid)
    }

    override fun existsByBlockerUuidAndBlockedUuid(blockerUuid: UUID, blockedUuid: UUID): Boolean {
        return jpaRepository.existsByBlockerUuidAndBlockedUuid(blockerUuid, blockedUuid)
    }
}
