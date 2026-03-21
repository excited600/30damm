package beyondeyesight.domain.repository.auth

import beyondeyesight.domain.model.auth.RefreshTokenEntity
import java.util.UUID

interface RefreshTokenRepository {
    fun save(entity: RefreshTokenEntity): RefreshTokenEntity
    fun findByToken(token: String): RefreshTokenEntity?
    fun deleteByToken(token: String)
    fun deleteAllByUserUuid(userUuid: UUID)
}
