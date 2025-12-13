package beyondeyesight.domain.model.payment

sealed class PaymentVerifyResult {
    data class Success(val payment: PaymentEntity) : PaymentVerifyResult()
    data class Failure(val message: String, val paymentId: String) : PaymentVerifyResult()
    data class Pending(val payment: PaymentEntity, val message: String) : PaymentVerifyResult()

    companion object {
        fun success(payment: PaymentEntity) = Success(payment)
        fun failure(message: String, paymentId: String) = Failure(message, paymentId)
        fun pending(payment: PaymentEntity, message: String) = Pending(payment, message)
    }

    val isSuccess: Boolean get() = this is Success
}