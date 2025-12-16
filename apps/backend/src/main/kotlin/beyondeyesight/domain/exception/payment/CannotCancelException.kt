package beyondeyesight.domain.exception.payment

import beyondeyesight.domain.exception.ServerException
import beyondeyesight.domain.model.payment.Status

class CannotCancelException private constructor(message: String): ServerException(statusCode = 500, message) {
    companion object {
        fun invalidPaymentStatus(paymentId: String, status: Status): CannotCancelException {
            return CannotCancelException("결제 상태가 이상하여 취소 불가. paymentId=$paymentId, status=$status")
        }
    }
}