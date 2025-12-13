package beyondeyesight.domain.model.user

import beyondeyesight.domain.model.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import beyondeyesight.config.uuidV7
import java.util.UUID

@Entity
@Table(name = "users")
class UserEntity(
    uuid: UUID,
    @Column(nullable = false, unique = true)
    val email: String,
    @Column(nullable = false)
    var nickname: String,
    @Column(nullable = true)
    var age: Int, // TODO: 생년월일을 아예 가지고 있는게 나을듯.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val gender: Gender,
    @Column(nullable = false)
    var introduction: String,
    @Column(nullable = false)
    var password: String,
    @Column(name = "phone_number", nullable = false)
    var phoneNumber: String,
    @Column(name = "phone_authenticated", nullable = false)
    var phoneAuthenticated: Boolean,
    @Column(nullable = false)
    var hearts: Int,
    @Column(name = "is_private", nullable = false)
    var isPrivate: Boolean,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val provider: Provider,

    ) : BaseEntity(uuid = uuid) {

    companion object {
        fun signUp(
            email: String,
            nickname: String,
            age: Int,
            gender: Gender,
            introduction: String,
            password: String,
            phoneNumber: String,
            phoneAuthenticated: Boolean,
        ): UserEntity {
            return UserEntity(
                uuid = uuidV7(),
                email = email,
                nickname = nickname,
                age = age,
                gender = gender,
                introduction = introduction,
                password = password,
                phoneNumber = phoneNumber,
                phoneAuthenticated = phoneAuthenticated,
                hearts = 0,
                isPrivate = false,
                provider = Provider.THIRTY_FORTY,
            )
        }
    }

    enum class Provider {
        THIRTY_FORTY,
        KAKAO;
    }
}