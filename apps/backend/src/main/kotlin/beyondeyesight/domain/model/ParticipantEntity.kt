package beyondeyesight.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable
import java.util.UUID

@Entity
@Table(name = "participants")
@IdClass(ParticipantId::class)
class ParticipantEntity(
    @Id
    @Column(name = "gathering_uuid", nullable = false)
    val gatheringUuid: UUID,
    @Id
    @Column(name = "user_uuid", nullable = false)
    val userUuid: UUID,
    @Column(name = "is_host", nullable = false)
    val isHost: Boolean
) {
    companion object {
        fun join(
            gatheringUuid: UUID,
            userUuid: UUID,
            isHost: Boolean
        ): ParticipantEntity {
            return ParticipantEntity(
                gatheringUuid = gatheringUuid,
                userUuid = userUuid,
                isHost = isHost
            )
        }
    }
}

data class ParticipantId(
    val gatheringUuid: UUID = UUID.randomUUID(),
    val userUuid: UUID = UUID.randomUUID()
) : Serializable
