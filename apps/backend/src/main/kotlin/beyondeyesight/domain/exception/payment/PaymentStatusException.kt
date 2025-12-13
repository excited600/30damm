package beyondeyesight.domain.exception.payment

import beyondeyesight.domain.exception.ServerException
import beyondeyesight.domain.model.payment.Status

class PaymentStatusException(message: String): ServerException(statusCode = 500, message = message) {

    companion object {
        fun cannotPay(currentStatus: Status): PaymentStatusException {
            return PaymentStatusException("Payment is available only when status is ${Status.PENDING}. current status: $currentStatus")
        }

        fun cannotFail(currentStatus: Status): PaymentStatusException {
            return PaymentStatusException("Fail is available only when status is ${Status.PENDING}. current status: $currentStatus")
        }

        fun cannotCancel(status: Status): PaymentStatusException {
            return PaymentStatusException("Cancel is available only when status is ${Status.PAID}. current status: $status")
        }


        fun cannotCancelPartially(status: Status): PaymentStatusException {
            return PaymentStatusException("Partial cancel is available only when status is ${Status.PAID}. current status: $status")
        }
    }
}