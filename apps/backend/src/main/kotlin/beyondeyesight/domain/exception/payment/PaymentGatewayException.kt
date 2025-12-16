package beyondeyesight.domain.exception.payment

import beyondeyesight.domain.exception.ServerException

class PaymentGatewayException private constructor(override val message: String): ServerException(500, message) {

    companion object {
        fun noResponse(): PaymentGatewayException {
            return PaymentGatewayException("요청에 대한 응답이 null")
        }

        fun getFail(errorMessage: String): PaymentGatewayException {
            return PaymentGatewayException("결제 조회 실패: $errorMessage")
        }

        fun cancelFail(errorMessage: String): PaymentGatewayException {
            return PaymentGatewayException("결제 취소 실패: $errorMessage")
        }
    }
}