package beyondeyesight.domain.repository.gathering

import beyondeyesight.domain.model.GuestEntity
import beyondeyesight.domain.model.GuestId
import beyondeyesight.domain.model.user.Gender
import java.util.UUID

interface GuestRepository {
    fun save(guestEntity: GuestEntity): GuestEntity

    fun countByGathering(gatheringUuid: UUID): Long

    fun countByGatheringAndGender(gatheringUuid: UUID, gender: Gender): Long

    fun existsByGuestId(guestId: GuestId): Boolean

    fun findByGuestId(guestId: GuestId): GuestEntity?

    fun deleteByUserUuidAndGatheringUuid(userUuid: UUID, gatheringUuid: UUID)
}