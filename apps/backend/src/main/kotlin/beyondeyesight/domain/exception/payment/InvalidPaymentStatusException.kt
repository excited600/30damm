package beyondeyesight.domain.exception.payment

import beyondeyesight.domain.exception.ServerException
import beyondeyesight.domain.model.payment.Status

class InvalidPaymentStatusException(message: String): ServerException(statusCode = 500, message = message) {

    companion object {
        fun cannotPay(currentStatus: Status): InvalidPaymentStatusException {
            return InvalidPaymentStatusException("Payment is available only when status is ${Status.PAY_PENDING}. current status: $currentStatus")
        }

        fun cannotFail(currentStatus: Status): InvalidPaymentStatusException {
            return InvalidPaymentStatusException("Fail is available only when status is ${Status.PAY_PENDING}. current status: $currentStatus")
        }

        fun cannotCancel(status: Status): InvalidPaymentStatusException {
            return InvalidPaymentStatusException("Cancel is available only when status is ${Status.PAID}. current status: $status")
        }


        fun cannotCancelPartially(status: Status): InvalidPaymentStatusException {
            return InvalidPaymentStatusException("Partial cancel is available only when status is ${Status.PAID}. current status: $status")
        }
    }
}