package beyondeyesight.domain.repository.gathering

import beyondeyesight.domain.model.gathering.SeriesScheduleEntity

interface SeriesScheduleRepository {
    fun save(seriesScheduleEntity: SeriesScheduleEntity): SeriesScheduleEntity
}