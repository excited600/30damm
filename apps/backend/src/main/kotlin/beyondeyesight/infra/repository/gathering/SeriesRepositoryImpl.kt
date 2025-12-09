package beyondeyesight.infra.repository.gathering

import beyondeyesight.domain.model.gathering.SeriesEntity
import beyondeyesight.domain.repository.gathering.SeriesRepository
import org.springframework.stereotype.Repository

@Repository
class SeriesRepositoryImpl(
    private val seriesJpaRepository: SeriesJpaRepository,
): SeriesRepository {
    override fun save(seriesEntity: SeriesEntity): SeriesEntity {
        return seriesJpaRepository.save(seriesEntity)
    }
}