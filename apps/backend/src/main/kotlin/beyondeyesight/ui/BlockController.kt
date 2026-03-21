package beyondeyesight.ui

import beyondeyesight.api.BlocksApiService
import beyondeyesight.application.BlockApplicationService
import beyondeyesight.config.currentUserUuid
import beyondeyesight.model.BlockUserRequest
import beyondeyesight.model.BlockUserResponse
import org.springframework.stereotype.Service

@Service
class BlockController(
    private val blockApplicationService: BlockApplicationService,
) : BlocksApiService {

    override fun blockUser(blockUserRequest: BlockUserRequest): BlockUserResponse {
        return blockApplicationService.blockUser(
            blockerUuid = currentUserUuid(),
            blockedUuid = blockUserRequest.blockedUserUuid,
            mapper = { entity ->
                BlockUserResponse(
                    uuid = entity.uuid,
                    blockedUserUuid = entity.blockedUuid,
                )
            }
        )
    }
}
