package beyondeyesight.domain.model.auth

import beyondeyesight.config.uuidV7
import beyondeyesight.domain.model.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "refresh_tokens")
class RefreshTokenEntity(
    uuid: UUID,

    @Column(name = "user_uuid", nullable = false)
    val userUuid: UUID,

    @Column(nullable = false, unique = true)
    val token: String,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: LocalDateTime,
) : BaseEntity(uuid = uuid) {

    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)

    companion object {
        fun create(userUuid: UUID, token: String, expiresAt: LocalDateTime): RefreshTokenEntity {
            return RefreshTokenEntity(
                uuid = uuidV7(),
                userUuid = userUuid,
                token = token,
                expiresAt = expiresAt,
            )
        }
    }
}
