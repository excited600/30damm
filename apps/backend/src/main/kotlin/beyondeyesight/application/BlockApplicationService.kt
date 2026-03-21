package beyondeyesight.application

import beyondeyesight.domain.model.block.UserBlockedUserEntity
import beyondeyesight.domain.service.block.BlockService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class BlockApplicationService(
    private val blockService: BlockService,
) {
    @Transactional
    fun <R> blockUser(
        blockerUuid: UUID,
        blockedUuid: UUID,
        mapper: (UserBlockedUserEntity) -> R,
    ): R {
        val entity = blockService.blockUser(
            blockerUuid = blockerUuid,
            blockedUuid = blockedUuid,
        )
        return mapper.invoke(entity)
    }
}
