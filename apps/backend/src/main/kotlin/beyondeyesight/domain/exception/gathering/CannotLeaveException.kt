package beyondeyesight.domain.exception.gathering

import beyondeyesight.domain.exception.ServerException
import beyondeyesight.domain.model.payment.Status

class CannotLeaveException private constructor(message: String): ServerException(statusCode = 500, message = message) {
    companion object {
        fun invalidPaymentStatus(paymentId: String, status: Status): CannotLeaveException {
            return CannotLeaveException("결제 상태가 이상합니다. paymentId=${paymentId}, status=$status")
        }
    }
}