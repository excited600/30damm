package beyondeyesight.domain.service.payment

import beyondeyesight.domain.model.payment.Currency
import beyondeyesight.domain.model.payment.Payment
import beyondeyesight.domain.model.payment.PaymentCancelResponse
import beyondeyesight.domain.model.payment.PaymentClientConfig
import beyondeyesight.domain.model.payment.PaymentFailed

interface PaymentGateway {
    fun getPayment(paymentId: String): Payment

    fun cancelPayment(
        paymentId: String,
        reason: String,
        amount: Int? = null  // null이면 전액 취소
    ): PaymentCancelResponse

    fun preRegisterPayment(
        paymentId: String,
        totalAmount: Int,
        currency: Currency,
    )

    fun getPaymentClientConfig(): PaymentClientConfig

    fun confirm(paymentId: String, paymentToken: String, txId: String, amount: Int)
}