package beyondeyesight.domain.model

import beyondeyesight.domain.model.gathering.GatheringEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "guests")
@IdClass(GuestId::class)
class GuestEntity(
    @Id
    @Column(name = "gathering_uuid", nullable = false)
    val gatheringUuid: UUID,
    @Id
    @Column(name = "user_uuid", nullable = false)
    val userUuid: UUID,
    @Column(name = "joined_at", nullable = false)
    val joinedAt: LocalDateTime,
) {
    companion object {
        fun join(
            gatheringUuid: UUID,
            userUuid: UUID,
        ): GuestEntity {
            return GuestEntity(
                gatheringUuid = gatheringUuid,
                userUuid = userUuid,
                joinedAt = LocalDateTime.now()
            )
        }
    }
}

data class GuestId(
    val gatheringUuid: UUID = UUID.randomUUID(),
    val userUuid: UUID = UUID.randomUUID()
) : Serializable