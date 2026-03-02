package beyondeyesight.domain.service

import beyondeyesight.domain.exception.InvalidValueException
import beyondeyesight.domain.exception.ResourceNotFoundException
import beyondeyesight.domain.model.user.UserEntity
import beyondeyesight.domain.repository.user.UserRepository
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
        password: String,
    ): UserEntity {
        if (userRepository.findByEmail(email) != null) {
            throw InvalidValueException(
                valueName = "email",
                value = email,
                reason = "이미 사용 중인 이메일입니다."
            )
        }

        val encoded = passwordEncoder.encode(password) ?: throw IllegalStateException("not allowed null")
        val entity = UserEntity.signUp(
            email = email,
            nickname = nickname,
            password = encoded,
        )

        return userRepository.save(entity)
    }

    fun login(
        email: String,
        password: String,
    ): UserEntity {
        val user = userRepository.findByEmail(email)
            ?: throw ResourceNotFoundException.byField(
                resourceName = UserEntity.RESOURCE_NAME,
                fieldName = "email",
                fieldValue = email
            )

        if (!passwordEncoder.matches(password, user.password)) {
            throw InvalidValueException(
                valueName = "password",
                value = "***",
                reason = "비밀번호가 일치하지 않습니다."
            )
        }

        return user
    }
}
