package beyondeyesight.infra.repository.gathering

import beyondeyesight.domain.model.gathering.SeriesEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SeriesJpaRepository: JpaRepository<SeriesEntity, UUID> {
}