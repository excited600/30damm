package beyondeyesight.infra.repository.block

import beyondeyesight.domain.model.block.UserBlockedUserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface UserBlockedUserJpaRepository : JpaRepository<UserBlockedUserEntity, UUID> {
    @Query("SELECT b.blockedUuid FROM UserBlockedUserEntity b WHERE b.blockerUuid = :blockerUuid")
    fun findBlockedUuidsByBlockerUuid(blockerUuid: UUID): List<UUID>

    fun existsByBlockerUuidAndBlockedUuid(blockerUuid: UUID, blockedUuid: UUID): Boolean
}
