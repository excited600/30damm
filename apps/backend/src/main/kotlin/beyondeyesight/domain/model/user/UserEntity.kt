package beyondeyesight.domain.model.user

import beyondeyesight.domain.model.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import beyondeyesight.config.uuidV7
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "users")
class UserEntity(
    uuid: UUID,
    @Column(nullable = false, unique = true)
    val email: String,
    @Column(nullable = false)
    var nickname: String,
    @Column(nullable = false)
    var age: Int,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val gender: Gender,
    @Column(nullable = true)
    var introduction: String?,
    @Column(nullable = false)
    var password: String,
    @Column(name = "phone_number", nullable = true)
    var phoneNumber: String?,
    @Column(name = "phone_authenticated", nullable = true)
    var phoneAuthenticated: Boolean?,
    @Column(nullable = false)
    var hearts: Int,
    @Column(name = "is_private", nullable = false)
    var isPrivate: Boolean,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val provider: Provider,
    @Column(name = "profile_image_url", nullable = true)
    var profileImageUrl: String?,
    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean,
    @Column(name = "deleted_at", nullable = true)
    var deletedAt: LocalDateTime?,

    ) : BaseEntity(uuid = uuid) {

    fun delete() {
        isDeleted = true
        deletedAt = LocalDateTime.now()
    }

    companion object {
        fun signUp(
            email: String,
            nickname: String,
            password: String,
        ): UserEntity {
            return UserEntity(
                uuid = uuidV7(),
                email = email,
                nickname = nickname,
                age = 30,
                gender = Gender.MALE,
                introduction = null,
                password = password,
                phoneNumber = null,
                phoneAuthenticated = null,
                hearts = 0,
                isPrivate = false,
                provider = Provider.THIRTY_FORTY,
                profileImageUrl = null,
                isDeleted = false,
                deletedAt = null,
            )
        }
        const val RESOURCE_NAME = "users"
    }

    enum class Provider {
        THIRTY_FORTY,
        KAKAO;
    }
}
