package beyondeyesight.domain.service

import beyondeyesight.domain.model.UserEntity
import beyondeyesight.domain.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    fun signUp(
        email: String,
        nickname: String,
        age: Int,
        gender: UserEntity.Gender,
        introduction: String,
        password: String,
        phoneNumber: String,
        phoneAuthenticated: Boolean,
    ): UserEntity {
        val encoded = passwordEncoder.encode(password) ?: throw IllegalStateException("not allowed null")
        val entity = UserEntity.signUp(
            email = email,
            nickname = nickname,
            age = age,
            gender = gender,
            introduction = introduction,
            password = encoded,
            phoneNumber = phoneNumber,
            phoneAuthenticated = phoneAuthenticated,
        )

        return userRepository.save(entity)
    }

}