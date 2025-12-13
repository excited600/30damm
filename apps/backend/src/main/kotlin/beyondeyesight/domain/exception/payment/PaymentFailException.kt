package beyondeyesight.domain.exception.payment

import beyondeyesight.domain.exception.ServerException

class PaymentFailException(override val message: String): ServerException(500, message) {

}