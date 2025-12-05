package beyondeyesight.infra.repository

import beyondeyesight.domain.model.GatheringEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface GatheringJpaRepository: JpaRepository<GatheringEntity, UUID> {
}