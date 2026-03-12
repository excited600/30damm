package beyondeyesight.application

import beyondeyesight.domain.model.user.UserEntity
import beyondeyesight.domain.service.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserApplicationService(
    private val userService: UserService,
) {
    @Transactional
    fun <R> signUp(
        email: String,
        nickname: String,
        password: String,
        mapper: (UserEntity) -> R
    ): R {
        val userEntity = userService.signUp(
            email = email,
            nickname = nickname,
            password = password,
        )
        return mapper.invoke(userEntity)
    }

    @Transactional(readOnly = true)
    fun <R> login(
        email: String,
        password: String,
        mapper: (UserEntity) -> R
    ): R {
        val userEntity = userService.login(
            email = email,
            password = password,
        )
        return mapper.invoke(userEntity)
    }

    @Transactional
    fun delete(userUuid: UUID) {
        userService.delete(userUuid)
    }
}
