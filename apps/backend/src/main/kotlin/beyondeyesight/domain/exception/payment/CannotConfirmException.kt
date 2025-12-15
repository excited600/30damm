package beyondeyesight.domain.exception.payment

import beyondeyesight.domain.exception.ClientException
import beyondeyesight.domain.model.payment.Status

class CannotConfirmException private constructor (override val message: String): ClientException(statusCode = 400, message = message) {
    companion object {
        fun invalidStatus(status: Status): CannotConfirmException {
            return CannotConfirmException("부적절한 현재 상태: $status")
        }
    }
}