package beyondeyesight.domain.repository

import beyondeyesight.domain.model.GuestEntity
import java.util.UUID

interface GuestRepository {
    fun save(guestEntity: GuestEntity): GuestEntity

    fun countByGatheringUuid(gatheringUuid: UUID): Long
}
