package beyondeyesight.domain.model.payment

import beyondeyesight.util.InstantToLocalDateTimeDeserializer
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.time.LocalDateTime

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "status"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = PaymentCancelled::class, name = "CANCELLED"),
    JsonSubTypes.Type(value = PaymentFailed::class, name = "FAILED"),
    JsonSubTypes.Type(value = PaymentPaid::class, name = "PAID"),
    JsonSubTypes.Type(value = PaymentPartialCancelled::class, name = "PARTIAL_CANCELLED"),
    JsonSubTypes.Type(value = PaymentPayPending::class, name = "PAY_PENDING"),
    JsonSubTypes.Type(value = PaymentReady::class, name = "READY"),
    JsonSubTypes.Type(value = PaymentVirtualAccountIssued::class, name = "VIRTUAL_ACCOUNT_ISSUED"),
)
@JsonIgnoreProperties(ignoreUnknown = true)
sealed interface Payment {
    val status: Status                // PAID, READY, FAILED, CANCELLED, PARTIAL_CANCELLED
    val id: String                         // 포트원 내부 결제 ID
    val transactionId: String
    val merchantId: String
    val amount: PaymentAmount
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentVirtualAccountIssued(
    override val status: Status = Status.VIRTUAL_ACCOUNT_ISSUED,
    override val id: String,
    override val transactionId: String,
    override val merchantId: String,
    override val amount: PaymentAmount,
): Payment

@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentReady(
    override val status: Status = Status.READY,
    override val id: String,
    override val transactionId: String,
    override val merchantId: String,
    override val amount: PaymentAmount,
): Payment

@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentPayPending(
    override val status: Status = Status.PAY_PENDING,
    override val id: String,
    override val transactionId: String,
    override val merchantId: String,
    override val amount: PaymentAmount,
): Payment

@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentPartialCancelled(
    override val status: Status = Status.PARTIAL_CANCELLED,
    override val id: String,
    override val transactionId: String,
    override val merchantId: String,
    override val amount: PaymentAmount,
    @JsonDeserialize(using = InstantToLocalDateTimeDeserializer::class)
    val cancelledAt: LocalDateTime,
    val cancellations: List<PaymentCancellation>
): Payment

@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentPaid(
    override val status: Status = Status.PAID,
    override val id: String,
    override val transactionId: String,
    override val merchantId: String,
    override val amount: PaymentAmount,
    @JsonDeserialize(using = InstantToLocalDateTimeDeserializer::class)
    val paidAt: LocalDateTime,
): Payment

@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentFailed(
    override val status: Status = Status.FAILED,
    override val id: String,
    override val transactionId: String,
    override val merchantId: String,
    override val amount: PaymentAmount,
    @JsonDeserialize(using = InstantToLocalDateTimeDeserializer::class)
    val failedAt: LocalDateTime,
    val failure: PaymentFailure,
): Payment

@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentFailure(
    val reason: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentCancelled(
    override val status: Status,
    override val id: String,
    override val transactionId: String,
    override val merchantId: String,
    val storeId: String,
    override val amount: PaymentAmount,
    @JsonDeserialize(using = InstantToLocalDateTimeDeserializer::class)
    val cancelledAt: LocalDateTime,
    val cancellations: List<PaymentCancellation>?
): Payment


@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentAmount(
    val total: Int,
    val paid: Int,
    val cancelled: Int,
    val taxFree: Int,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentCancellation(
    val status: PaymentCancellationStatus,
    val id: String,
    val totalAmount: Int,
    val reason: String,

    @JsonDeserialize(using = InstantToLocalDateTimeDeserializer::class)
    val cancelledAt: LocalDateTime?
)

enum class PaymentCancellationStatus {
    FAILED,
    REQUESTED,
    SUCCEEDED,
}

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
    val totalAmount: Int // TODO: nullable인지 확인하기
)