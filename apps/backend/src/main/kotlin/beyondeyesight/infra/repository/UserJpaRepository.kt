package beyondeyesight.infra.repository

import beyondeyesight.domain.model.User.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserJpaRepository: JpaRepository<UserEntity, UUID> {
}