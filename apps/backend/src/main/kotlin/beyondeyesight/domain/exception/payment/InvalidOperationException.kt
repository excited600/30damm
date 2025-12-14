package beyondeyesight.domain.exception.payment

import beyondeyesight.domain.exception.ClientException

class InvalidOperationException private constructor(override val message: String): ClientException(
    statusCode = 400,
    message = message
) {
    companion object {
        fun cannotCancel(reason: String): InvalidOperationException {
            return InvalidOperationException("취소 불가. 이유: $reason")
        }
    }
}