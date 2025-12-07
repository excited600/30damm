package beyondeyesight.infra.repository.gathering

import beyondeyesight.domain.model.gathering.SeriesEntity
import beyondeyesight.domain.model.gathering.SeriesScheduleEntity
import beyondeyesight.domain.repository.gathering.SeriesRepository
import beyondeyesight.domain.repository.gathering.SeriesScheduleRepository
import org.springframework.stereotype.Repository

@Repository
class SeriesScheduleRepositoryAdapter(
    private val seriesScheduleJpaRepository: SeriesScheduleJpaRepository,
): SeriesScheduleRepository {
    override fun save(seriesScheduleEntity: SeriesScheduleEntity): SeriesScheduleEntity {
        return seriesScheduleJpaRepository.save(seriesScheduleEntity)
    }
}