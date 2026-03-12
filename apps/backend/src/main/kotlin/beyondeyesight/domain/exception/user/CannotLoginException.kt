package beyondeyesight.domain.exception.user

import beyondeyesight.domain.exception.ClientException

class CannotLoginException private constructor(message: String) : ClientException(400, message) {
    companion object {
        fun userIsDeleted(): CannotLoginException {
            return CannotLoginException("Cannot login because the user has been deleted.")
        }
    }
}