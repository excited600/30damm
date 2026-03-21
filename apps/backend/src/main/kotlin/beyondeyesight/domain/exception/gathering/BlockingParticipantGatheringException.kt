package beyondeyesight.domain.exception.gathering

import beyondeyesight.domain.exception.ClientException
import java.util.UUID

class BlockingParticipantGatheringException(
    message: String
) : ClientException(
    statusCode = 403,
    message = message
) {
    companion object {
        fun blockedHost(gatheringUuid: UUID): BlockingParticipantGatheringException {
            return BlockingParticipantGatheringException(
                "Cannot view gathering $gatheringUuid because the host is blocked."
            )
        }

        fun blockedGuest(gatheringUuid: UUID): BlockingParticipantGatheringException {
            return BlockingParticipantGatheringException(
                "Cannot view gathering $gatheringUuid because a blocked user is participating."
            )
        }
    }
}
