package beyondeyesight.infra.repository

import beyondeyesight.domain.model.GatheringEntity
import beyondeyesight.domain.repository.GatheringRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
class GatheringJpaRepositoryAdapterI(
    private val gatheringJpaRepository: GatheringJpaRepository,
): GatheringRepository {
    override fun create(gatheringEntity: GatheringEntity): GatheringEntity {
        return gatheringJpaRepository.save(gatheringEntity)
    }

    override fun findByUuid(uuid: UUID): GatheringEntity? {
        return gatheringJpaRepository.findById(uuid).orElse(null)
    }

    override fun delete(gatheringEntity: GatheringEntity) {
        gatheringJpaRepository.delete(gatheringEntity)
    }

    override fun delete(uuid: UUID) {
        gatheringJpaRepository.deleteById(uuid)
    }

    override fun save(gatheringEntity: GatheringEntity): GatheringEntity {
        return gatheringJpaRepository.save(gatheringEntity)
    }
}