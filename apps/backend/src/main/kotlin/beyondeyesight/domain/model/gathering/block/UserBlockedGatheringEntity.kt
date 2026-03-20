package beyondeyesight.domain.model.gathering.block

import beyondeyesight.config.uuidV7
import beyondeyesight.domain.model.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.util.UUID

@Entity
@Table(
    name = "user_blocked_gatherings",
    uniqueConstraints = [
        UniqueConstraint(
            name = "unique_user_blocked_gathering",
            columnNames = ["user_uuid", "gathering_uuid"]
        )
    ]
)
class UserBlockedGatheringEntity(
    uuid: UUID,

    @Column(name = "user_uuid", nullable = false)
    val userUuid: UUID,

    @Column(name = "gathering_uuid", nullable = false)
    val gatheringUuid: UUID,
) : BaseEntity(uuid = uuid) {

    companion object {
        fun create(userUuid: UUID, gatheringUuid: UUID): UserBlockedGatheringEntity {
            return UserBlockedGatheringEntity(
                uuid = uuidV7(),
                userUuid = userUuid,
                gatheringUuid = gatheringUuid,
            )
        }
    }
}
