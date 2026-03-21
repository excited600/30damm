package beyondeyesight.ui

import beyondeyesight.api.UsersApiService
import beyondeyesight.application.UserApplicationService
import beyondeyesight.config.currentUserUuid
import beyondeyesight.model.*
import org.springframework.stereotype.Service

@Service
class UserController(
    private val userApplicationService: UserApplicationService,
) : UsersApiService {

    override fun signup(signupRequest: SignupRequest): SignupResponse {
        return userApplicationService.signUp(
            email = signupRequest.email,
            nickname = signupRequest.nickname,
            password = signupRequest.password,
            mapper = { userEntity, tokenPair ->
                SignupResponse(
                    userUuid = userEntity.uuid,
                    accessToken = tokenPair.accessToken,
                    refreshToken = tokenPair.refreshToken,
                )
            }
        )
    }

    override fun login(loginRequest: LoginRequest): LoginResponse {
        return userApplicationService.login(
            email = loginRequest.email,
            password = loginRequest.password,
            mapper = { userEntity, tokenPair ->
                LoginResponse(
                    userUuid = userEntity.uuid,
                    accessToken = tokenPair.accessToken,
                    refreshToken = tokenPair.refreshToken,
                )
            }
        )
    }

    override fun refreshToken(refreshTokenRequest: RefreshTokenRequest): RefreshTokenResponse {
        return userApplicationService.refreshToken(
            refreshToken = refreshTokenRequest.refreshToken,
            mapper = { tokenPair ->
                RefreshTokenResponse(
                    accessToken = tokenPair.accessToken,
                    refreshToken = tokenPair.refreshToken,
                )
            }
        )
    }

    override fun deleteUser() {
        userApplicationService.delete(userUuid = currentUserUuid())
    }
}
