package beyondeyesight.domain.exception.gathering

import beyondeyesight.domain.exception.ClientException
import beyondeyesight.domain.model.User.Gender
import java.util.UUID

class CannotJoinException(
    message: String
    // TODO: 에러코드
) : ClientException(
    statusCode = 400,
    message = message
) {
    companion object {
        fun full(userUuid: UUID, gatheringUuid: UUID, gender: Gender): CannotJoinException {
            return CannotJoinException(
                "User with uuid $userUuid cannot join gathering with uuid " +
                        "$gatheringUuid because it is $gender full."
            )
        }

        fun full(userUuid: UUID, gatheringUuid: UUID): CannotJoinException {
            return CannotJoinException(
                "User with uuid $userUuid cannot join gathering with uuid " +
                        "$gatheringUuid because it is full."
            )
        }
    }
}