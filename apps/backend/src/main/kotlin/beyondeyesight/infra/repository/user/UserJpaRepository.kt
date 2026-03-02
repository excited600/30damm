package beyondeyesight.infra.repository.user

import beyondeyesight.domain.model.user.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserJpaRepository: JpaRepository<UserEntity, UUID> {
    fun findByEmail(email: String): UserEntity?
}