package beyondeyesight.domain.model.payment

import java.time.LocalDateTime

data class Webhook(
    val type: WebhookType,
    val timestamp: LocalDateTime,
    val data: WebhookData
)

data class WebhookData(
    val storeId: String,
    val paymentId: String,
    val transactionId: String,
    val cancellationId: String?
)

enum class WebhookType {
    TransactionPeriodReady,
    TransactionPeriodPaid,
    TransactionPeriodVirtualAccountIssued,
    TransactionPeriodPartialCancelled,
    TransactionPeriodCancelled,
    TransactionPeriodFailed,
    TransactionPeriodPayPending,
    TransactionPeriodCancelPending,
    TransactionPeriodDisputeCreated,
    TransactionPeriodDisputeResolved,
}