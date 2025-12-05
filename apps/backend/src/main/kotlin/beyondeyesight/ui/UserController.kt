package beyondeyesight.ui

import beyondeyesight.application.UserApplicationService
import beyondeyesight.domain.model.UserEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping(("/users"))
class UserController(
    private val userApplicationService: UserApplicationService,
) {

    @PostMapping("/")
    fun signUp(@RequestBody request: SignUpRequest): SignUpResponse {
        return userApplicationService.signUp(
            email = request.email,
            nickname = request.nickname,
            age = request.age,
            gender = request.gender,
            introduction = request.introduction,
            password = request.password,
            phoneNumber = request.phoneNumber,
            phoneAuthenticated = request.phoneAuthenticated,
            mapper = {userEntity: UserEntity -> SignUpResponse.from(userEntity)}
        )
    }

    class SignUpRequest(
        val email: String,
        val nickname: String,
        val age: Int,
        val gender: UserEntity.Gender,
        val introduction: String,
        val password: String,
        val phoneNumber: String,
        val phoneAuthenticated: Boolean,
    )

    class SignUpResponse(
        val uuid: UUID,
        val email: String,
        val nickname: String,
        val age: Int,
        val gender: UserEntity.Gender,
        val introduction: String,
        val phoneNumber: String,
        val phoneAuthenticated: Boolean,
        val hearts: Int,
        val isPrivate: Boolean,
        val provider: UserEntity.Provider
    ) {
        companion object {
            fun from(userEntity: UserEntity): SignUpResponse {
                return SignUpResponse(
                    uuid = userEntity.uuid,
                    email = userEntity.email,
                    nickname = userEntity.nickname,
                    age = userEntity.age,
                    gender = userEntity.gender,
                    introduction = userEntity.introduction,
                    phoneNumber = userEntity.phoneNumber,
                    phoneAuthenticated = userEntity.phoneAuthenticated,
                    hearts = userEntity.hearts,
                    isPrivate = userEntity.isPrivate,
                    provider = userEntity.provider
                )
            }
        }
    }
}