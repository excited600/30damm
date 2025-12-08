package beyondeyesight.infra.repository

import beyondeyesight.domain.model.GuestEntity
import beyondeyesight.domain.model.User.Gender
import beyondeyesight.domain.model.User.UserEntity
import beyondeyesight.domain.model.gathering.GatheringEntity
import beyondeyesight.domain.repository.GuestRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class GuestRepositoryImpl(
    private val guestJpaRepository: GuestJpaRepository
) : GuestRepository {
    override fun save(guestEntity: GuestEntity): GuestEntity {
        return guestJpaRepository.save(guestEntity)
    }

    override fun countByGathering(gatheringUuid: UUID): Long {
        return guestJpaRepository.findAll {
            select(count(entity(GuestEntity::class)))
                .from(
                    entity(GuestEntity::class)
                )
                .where(
                    path(GuestEntity::gatheringUuid).eq(gatheringUuid)
                )
        }.firstOrNull()?: 0L
    }

    override fun countByGatheringAndGender(
        gatheringUuid: UUID,
        gender: Gender
    ): Long {
        return guestJpaRepository.findAll {
            select(count(entity(GuestEntity::class)))
                .from(
                    entity(GuestEntity::class),
                    join(UserEntity::class).on(
                        path(UserEntity::uuid)
                            .eq(path(GuestEntity::userUuid))
                    )
                )
                .where(
                    and(
                        path(GuestEntity::gatheringUuid).eq(gatheringUuid),
                        path(UserEntity::gender).eq(gender),
                    )
                )
        }.firstOrNull()?: 0L
    }
}
