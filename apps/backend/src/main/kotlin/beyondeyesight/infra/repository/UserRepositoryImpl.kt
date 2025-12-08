package beyondeyesight.infra.repository

import beyondeyesight.domain.model.User.UserEntity
import beyondeyesight.domain.repository.UserRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserRepositoryImpl(
    private val userJpaRepository: UserJpaRepository,
): UserRepository {
    override fun save(userEntity: UserEntity): UserEntity {
        return userJpaRepository.save(userEntity)
    }

    override fun findByUuid(uuid: UUID): UserEntity? {
        return userJpaRepository.findById(uuid).orElse(null)
    }
}