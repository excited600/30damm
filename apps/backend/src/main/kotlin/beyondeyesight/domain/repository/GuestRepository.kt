package beyondeyesight.domain.repository

import beyondeyesight.domain.model.GuestEntity
import beyondeyesight.domain.model.User.Gender
import beyondeyesight.domain.model.gathering.GatheringEntity
import java.util.UUID

interface GuestRepository {
    fun save(guestEntity: GuestEntity): GuestEntity

    fun countByGathering(gatheringUuid: UUID): Long

    fun countByGatheringAndGender(gatheringUuid: UUID, gender: Gender): Long
}
