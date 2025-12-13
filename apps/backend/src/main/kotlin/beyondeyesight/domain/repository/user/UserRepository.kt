package beyondeyesight.domain.repository.user

import beyondeyesight.domain.model.user.UserEntity
import java.util.UUID

interface UserRepository {
    fun save(userEntity: UserEntity): UserEntity

    fun findByUuid(uuid: UUID): UserEntity?
}