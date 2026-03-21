package beyondeyesight.application

import beyondeyesight.domain.model.user.UserEntity
import beyondeyesight.domain.service.UserService
import beyondeyesight.domain.service.auth.AuthService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserApplicationService(
    private val userService: UserService,
    private val authService: AuthService,
) {
    @Transactional
    fun <R> signUp(
        email: String,
        nickname: String,
        password: String,
        mapper: (UserEntity, AuthService.TokenPair) -> R
    ): R {
        val userEntity = userService.signUp(
            email = email,
            nickname = nickname,
            password = password,
        )
        val tokenPair = authService.issueTokens(userEntity.uuid)
        return mapper.invoke(userEntity, tokenPair)
    }

    @Transactional
    fun <R> login(
        email: String,
        password: String,
        mapper: (UserEntity, AuthService.TokenPair) -> R
    ): R {
        val userEntity = userService.login(
            email = email,
            password = password,
        )
        val tokenPair = authService.issueTokens(userEntity.uuid)
        return mapper.invoke(userEntity, tokenPair)
    }

    @Transactional
    fun <R> refreshToken(
        refreshToken: String,
        mapper: (AuthService.TokenPair) -> R
    ): R {
        val tokenPair = authService.refresh(refreshToken)
        return mapper.invoke(tokenPair)
    }

    @Transactional
    fun delete(userUuid: UUID) {
        authService.revokeAllTokens(userUuid)
        userService.delete(userUuid)
    }
}
