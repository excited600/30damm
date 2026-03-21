package beyondeyesight.domain.model.block

import beyondeyesight.config.uuidV7
import beyondeyesight.domain.model.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.util.UUID

@Entity
@Table(
    name = "user_blocked_users",
    uniqueConstraints = [
        UniqueConstraint(
            name = "unique_user_blocked_user",
            columnNames = ["blocker_uuid", "blocked_uuid"]
        )
    ]
)
class UserBlockedUserEntity(
    uuid: UUID,

    @Column(name = "blocker_uuid", nullable = false)
    val blockerUuid: UUID,

    @Column(name = "blocked_uuid", nullable = false)
    val blockedUuid: UUID,
) : BaseEntity(uuid = uuid) {

    companion object {
        fun create(blockerUuid: UUID, blockedUuid: UUID): UserBlockedUserEntity {
            return UserBlockedUserEntity(
                uuid = uuidV7(),
                blockerUuid = blockerUuid,
                blockedUuid = blockedUuid,
            )
        }
    }
}
