package beyondeyesight.ui

import beyondeyesight.api.UsersApiService
import beyondeyesight.application.UserApplicationService
import beyondeyesight.config.JwtTokenProvider
import beyondeyesight.model.LoginRequest
import beyondeyesight.model.LoginResponse
import beyondeyesight.model.SignupRequest
import beyondeyesight.model.SignupResponse
import org.springframework.stereotype.Service

@Service
class UserController(
    private val userApplicationService: UserApplicationService,
    private val jwtTokenProvider: JwtTokenProvider,
) : UsersApiService {

    override fun signup(signupRequest: SignupRequest): SignupResponse {
        return userApplicationService.signUp(
            email = signupRequest.email,
            nickname = signupRequest.nickname,
            password = signupRequest.password,
            mapper = { userEntity ->
                SignupResponse(
                    userUuid = userEntity.uuid,
                    accessToken = jwtTokenProvider.generateAccessToken(userEntity.uuid),
                    refreshToken = jwtTokenProvider.generateRefreshToken(userEntity.uuid),
                )
            }
        )
    }

    override fun login(loginRequest: LoginRequest): LoginResponse {
        return userApplicationService.login(
            email = loginRequest.email,
            password = loginRequest.password,
            mapper = { userEntity ->
                LoginResponse(
                    userUuid = userEntity.uuid,
                    accessToken = jwtTokenProvider.generateAccessToken(userEntity.uuid),
                    refreshToken = jwtTokenProvider.generateRefreshToken(userEntity.uuid),
                )
            }
        )
    }
}
