package beyondeyesight.domain.exception.payment

import beyondeyesight.domain.exception.ServerException
import beyondeyesight.domain.model.payment.Status

class VerificationFailedException private constructor(override val message: String): ServerException(
    statusCode = 500,
    message = message) {

    companion object {


        fun invalidAmount(serverAmount: Int, pgAmount: Int): VerificationFailedException {
            return VerificationFailedException("결제 금액 불일치: 서버 금액 $serverAmount, PG 금액 $pgAmount")
        }
    }
}
