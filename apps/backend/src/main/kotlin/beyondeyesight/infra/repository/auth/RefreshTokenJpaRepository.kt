package beyondeyesight.infra.repository.auth

import beyondeyesight.domain.model.auth.RefreshTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RefreshTokenJpaRepository : JpaRepository<RefreshTokenEntity, UUID> {
    fun findByToken(token: String): RefreshTokenEntity?
    fun deleteByToken(token: String)
    fun deleteAllByUserUuid(userUuid: UUID)
}
