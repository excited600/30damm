package beyondeyesight.domain.service.payment

import beyondeyesight.domain.model.payment.Currency
import beyondeyesight.domain.model.payment.PaymentCancelResponse
import beyondeyesight.domain.model.payment.PaymentClientConfig
import beyondeyesight.domain.model.payment.PaymentDto

interface PaymentGateway {
    suspend fun getPayment(paymentId: String): PaymentDto

    suspend fun cancelPayment(
        paymentId: String,
        reason: String,
        amount: Int? = null  // null이면 전액 취소
    ): PaymentCancelResponse

    suspend fun preRegisterPayment(
        paymentId: String,
        totalAmount: Int,
        currency: Currency,
    )

    fun getPaymentClientConfig(): PaymentClientConfig
}