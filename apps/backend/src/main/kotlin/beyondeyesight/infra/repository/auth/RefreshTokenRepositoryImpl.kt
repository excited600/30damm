package beyondeyesight.infra.repository.auth

import beyondeyesight.domain.model.auth.RefreshTokenEntity
import beyondeyesight.domain.repository.auth.RefreshTokenRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class RefreshTokenRepositoryImpl(
    private val jpaRepository: RefreshTokenJpaRepository,
) : RefreshTokenRepository {

    override fun save(entity: RefreshTokenEntity): RefreshTokenEntity {
        return jpaRepository.save(entity)
    }

    override fun findByToken(token: String): RefreshTokenEntity? {
        return jpaRepository.findByToken(token)
    }

    override fun deleteByToken(token: String) {
        jpaRepository.deleteByToken(token)
    }

    override fun deleteAllByUserUuid(userUuid: UUID) {
        jpaRepository.deleteAllByUserUuid(userUuid)
    }
}
