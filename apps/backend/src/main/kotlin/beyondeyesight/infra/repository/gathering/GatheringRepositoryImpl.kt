package beyondeyesight.infra.repository.gathering

import beyondeyesight.domain.model.GuestEntity
import beyondeyesight.domain.model.ScrollResult
import beyondeyesight.domain.model.gathering.GatheringCursor
import beyondeyesight.domain.model.gathering.GatheringEntity
import beyondeyesight.domain.model.gathering.GatheringFilter
import beyondeyesight.domain.model.gathering.Status
import beyondeyesight.domain.repository.gathering.GatheringRepository
import com.linecorp.kotlinjdsl.dsl.jpql.Jpql
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicate
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalTime
import java.util.*

@Repository
class GatheringRepositoryImpl(
    private val gatheringJpaRepository: GatheringJpaRepository,
) : GatheringRepository {

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

    override fun scroll(
        cursor: GatheringCursor?,
        size: Int,
        filter: GatheringFilter,
    ): ScrollResult<GatheringEntity, GatheringCursor> {
        val results = gatheringJpaRepository.findPage(pageable = Pageable.ofSize(size + 1)) {
            select(entity(GatheringEntity::class))
                .from(entity(GatheringEntity::class))
                .whereAnd(
                    cursor?.let {
                        or(
                            path(GatheringEntity::score).lessThan(it.score),
                            and(
                                path(GatheringEntity::score).eq(it.score),
                                path(GatheringEntity::uuid).greaterThan(it.uuid)
                            )
                        )
                    },
                    or(
                        path(GatheringEntity::status).eq(Status.OPEN),
                        path(GatheringEntity::status).eq(Status.IN_PROGRESS)
                    ),
                    * buildFilterPredicates(filter).toTypedArray()
                )
                .orderBy(
                    path(GatheringEntity::score).desc(),
                    path(GatheringEntity::uuid).asc()
                )
        }.content.filterNotNull()

        val hasNext = results.size > size
        val items = results.take(size)

        return ScrollResult(
            items = items,
            cursor = items.lastOrNull()?.let {
                GatheringCursor(
                    score = it.score,
                    uuid = it.uuid
                )
            },
            hasNext = hasNext
        )
    }

    private fun Jpql.buildFilterPredicates(
        filter: GatheringFilter?
    ): List<Predicate?> {
        return listOf(
            filter?.categories?.takeIf { it.isNotEmpty() }?.let {
                path(GatheringEntity::category).`in`(it)
            },
            filter?.guestCount?.let { minGuestCount ->
                val guestCountSubquery = select(count(entity(GuestEntity::class)))
                    .from(entity(GuestEntity::class))
                    .where(path(GuestEntity::gatheringUuid).eq(path(GatheringEntity::uuid)))
                    .asSubquery()

                guestCountSubquery.greaterThanOrEqualTo(minGuestCount.toLong())
            },
            filter?.dayOfWeek?.let {
                path(GatheringEntity::dayOfWeek).eq(it)
            },
            filter?.startDate?.let {
                path(GatheringEntity::startDateTime).greaterThanOrEqualTo(it.atStartOfDay())
            },
            filter?.endDate?.let {
                path(GatheringEntity::startDateTime).lessThanOrEqualTo(it.atTime(LocalTime.MAX))
            },
            // TODO: 지도 api 쓰고 어떻게 저장되는지 보고... LIKE는 인덱스 안탈듯?
            filter?.location?.let {
                path(GatheringEntity::place).like("%$it%")
            },
            filter?.startAge?.let {
                path(GatheringEntity::minAge).greaterThanOrEqualTo(it)
            },
            filter?.endAge?.let {
                path(GatheringEntity::maxAge).lessThanOrEqualTo(it)
            },
            filter?.genderRatioEnabled?.let {
                path(GatheringEntity::genderRatioEnabled).eq(it)
            },
            filter?.minCapacity?.let {
                path(GatheringEntity::minCapacity).greaterThanOrEqualTo(it)
            },
            filter?.maxCapacity?.let {
                path(GatheringEntity::maxCapacity).lessThanOrEqualTo(it)
            },
            filter?.minFee?.let {
                path(GatheringEntity::fee).greaterThanOrEqualTo(it)
            },
            filter?.maxFee?.let {
                path(GatheringEntity::fee).lessThanOrEqualTo(it)
            }
        )
    }
}