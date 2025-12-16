package beyondeyesight.domain.model

import beyondeyesight.domain.model.gathering.GatheringEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable
import java.time.LocalDateTime
import beyondeyesight.config.uuidV7
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
//    TODO: status로 APPROVAL_PENDING, PAYMENT_PENDING, JOINED 등 상태 관리 필요
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
        const val RESOURCE_NAME = "guests"
    }
}

data class GuestId(
    val gatheringUuid: UUID = uuidV7(),
    val userUuid: UUID = uuidV7()
) : Serializable