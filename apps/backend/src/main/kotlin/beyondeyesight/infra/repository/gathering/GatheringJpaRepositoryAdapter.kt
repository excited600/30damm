package beyondeyesight.infra.repository.gathering

import beyondeyesight.domain.model.gathering.GatheringEntity
import beyondeyesight.domain.repository.gathering.GatheringRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class GatheringJpaRepositoryAdapterI(
    private val gatheringJpaRepository: GatheringJpaRepository,
): GatheringRepository {

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