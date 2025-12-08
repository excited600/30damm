package beyondeyesight.domain.repository

import beyondeyesight.domain.model.User.UserEntity
import java.util.UUID

interface UserRepository {
    fun save(userEntity: UserEntity): UserEntity

    fun findByUuid(uuid: UUID): UserEntity?
}