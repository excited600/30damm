package beyondeyesight.infra.repository.block

import beyondeyesight.domain.model.gathering.block.UserBlockedGatheringEntity
import beyondeyesight.domain.repository.gathering.block.UserBlockedGatheringRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserBlockedGatheringRepositoryImpl(
    private val jpaRepository: UserBlockedGatheringJpaRepository,
) : UserBlockedGatheringRepository {

    override fun save(entity: UserBlockedGatheringEntity): UserBlockedGatheringEntity {
        return jpaRepository.save(entity)
    }

    override fun findBlockedGatheringUuids(userUuid: UUID): List<UUID> {
        return jpaRepository.findGatheringUuidsByUserUuid(userUuid)
    }
}
