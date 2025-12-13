package beyondeyesight.domain.model.payment

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true) //TODO: jsonIgnoreProperties 뭔지 알기
data class PaymentDto(
    val id: String,                          // 포트원 내부 결제 ID

    @JsonProperty("transactionId")
    val transactionId: String? = null,

    @JsonProperty("merchantId")
    val merchantId: String? = null,

    val status: String,                       // PAID, READY, FAILED, CANCELLED, PARTIAL_CANCELLED

    val amount: PaymentAmount? = null,

    @JsonProperty("paidAt")
    val paidAt: String? = null,               // ISO 8601 형식

    @JsonProperty("cancelledAt")
    val cancelledAt: String? = null,

    @JsonProperty("failedAt")
    val failedAt: String? = null,

    @JsonProperty("failureReason")
    val failureReason: String? = null,

    val cancellations: List<PaymentCancellation>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentAmount(
    val total: Int,
    val paid: Int? = null,
    val cancelled: Int? = null,
    val taxFree: Int? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentCancellation(
    val id: String,
    val totalAmount: Long,
    val reason: String,
    val cancelledAt: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentCancelResponse(
    val cancellation: PaymentCancellation? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Webhook(
    val type: String,
    val timestamp: String? = null,
    val data: WebhookData? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class WebhookData(
    @JsonProperty("paymentId")
    val paymentId: String? = null,

    @JsonProperty("transactionId")
    val transactionId: String? = null,

    val status: String? = null,

    @JsonProperty("totalAmount")
    val totalAmount: Long? = null
)