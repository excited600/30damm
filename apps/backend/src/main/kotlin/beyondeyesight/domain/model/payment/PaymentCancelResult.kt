package beyondeyesight.domain.model.payment

sealed class PaymentCancelResult {
    data class Success(val payment: PaymentEntity) : PaymentCancelResult()
    data class Failure(val message: String) : PaymentCancelResult()

    companion object {
        fun success(payment: PaymentEntity) = Success(payment)
        fun failure(message: String) = Failure(message)
    }
}