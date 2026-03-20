package beyondeyesight.infra.repository.block

import beyondeyesight.domain.model.gathering.block.UserBlockedGatheringEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface UserBlockedGatheringJpaRepository : JpaRepository<UserBlockedGatheringEntity, UUID> {
    @Query("SELECT b.gatheringUuid FROM UserBlockedGatheringEntity b WHERE b.userUuid = :userUuid")
    fun findGatheringUuidsByUserUuid(userUuid: UUID): List<UUID>
}
