package beyondeyesight.domain.repository.gathering

import beyondeyesight.domain.model.gathering.SeriesEntity

interface SeriesRepository {

    fun save(seriesEntity: SeriesEntity): SeriesEntity
}