package beyondeyesight.infra.repository.gathering

import beyondeyesight.domain.model.GuestEntity
import beyondeyesight.domain.model.GuestId
import beyondeyesight.domain.model.user.Gender
import beyondeyesight.domain.model.user.UserEntity
import beyondeyesight.domain.repository.gathering.GuestRepository
import org.springframework.stereotype.Repository
import java.util.*

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

    override fun existsByUserUuidAndGatheringUuid(
        userUuid: UUID,
        gatheringUuid: UUID
    ): Boolean {
        return guestJpaRepository.existsByUserUuidAndGatheringUuid(
            userUuid = userUuid,
            gatheringUuid = gatheringUuid
        )
    }

    override fun deleteByUserUuidAndGatheringUuid(userUuid: UUID, gatheringUuid: UUID) {
        guestJpaRepository.deleteById(GuestId(
            gatheringUuid = gatheringUuid,
            userUuid = userUuid
        ))
    }

}
