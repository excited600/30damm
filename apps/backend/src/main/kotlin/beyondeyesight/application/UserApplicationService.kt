package beyondeyesight.application

import beyondeyesight.domain.model.UserEntity
import beyondeyesight.domain.service.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserApplicationService(
    private val userService: UserService,
) {
    @Transactional
    fun <R> signUp(
        email: String,
        nickname: String,
        age: Int,
        gender: UserEntity.Gender,
        introduction: String,
        password: String,
        phoneNumber: String,
        phoneAuthenticated: Boolean,
        mapper: (UserEntity) -> R
    ): R {
        val userEntity = userService.signUp(
            email = email,
            nickname = nickname,
            age = age,
            gender = gender,
            introduction = introduction,
            password = password,
            phoneNumber = phoneNumber,
            phoneAuthenticated = phoneAuthenticated,
        )

        return mapper.invoke(userEntity)
    }
}