package beyondeyesight.infra.repository.gathering

import beyondeyesight.domain.model.gathering.SeriesScheduleEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SeriesScheduleJpaRepository: JpaRepository<SeriesScheduleEntity, UUID> {
    fun save(seriesScheduleEntity: SeriesScheduleEntity): SeriesScheduleEntity
}