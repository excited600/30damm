package beyondeyesight.infra.repository

import beyondeyesight.domain.model.GuestEntity
import beyondeyesight.domain.repository.GuestRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class GuestJpaRepositoryAdapter(
    private val guestJpaRepository: GuestJpaRepository
) : GuestRepository {
    override fun save(guestEntity: GuestEntity): GuestEntity {
        return guestJpaRepository.save(guestEntity)
    }

    override fun countByGatheringUuid(gatheringUuid: UUID): Long {
        return guestJpaRepository.countByGatheringUuid(gatheringUuid)
    }
}
