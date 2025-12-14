package beyondeyesight.ui

import beyondeyesight.application.PaymentApplicationService
import beyondeyesight.domain.exception.InvalidValueException
import beyondeyesight.domain.model.payment.*
import beyondeyesight.domain.model.payment.Currency
import beyondeyesight.domain.service.payment.PaymentService
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/payments")
class PaymentController(
    private val paymentService: PaymentService,
    private val paymentApplicationService: PaymentApplicationService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)


    /**
     * 결제 준비 - 프론트에서 결제창 띄우기 전 호출
     */
    @PostMapping("/prepare")
    fun preparePayment(
        @RequestBody request:
        PreparePaymentApiRequest
    ): ResponseEntity<PreparePaymentApiResponse> {
        val result = paymentApplicationService.preparePayment(
            paymentId = request.paymentId,
            productType = request.productType,
            productUuid = request.productUuid,
            amount = request.amount,
            productName = request.productName,
            buyerEmail = request.buyerEmail,
            buyerName = request.buyerName,
            buyerPhone = request.buyerPhone
        )

        return ResponseEntity.ok(
            PreparePaymentApiResponse(
                success = true,
                paymentId = result.paymentId,
                storeId = result.storeId,
                channelKey = result.channelKey,
                orderName = result.productName,
                amount = result.amount,
                currency = result.currency
            )
        )
    }

    /**
     * 결제 검증 - 프론트에서 결제 완료 후 호출
     */
    @PostMapping("/verify")
    fun verifyPayment(
        @RequestBody request: VerifyPaymentRequest
    ): ResponseEntity<PaymentApiResponse> {

        val result = paymentApplicationService.verifyPayment(request.paymentId)
        val response = PaymentApiResponse(
            success = result.success,
            message = result.message,
            payment = result.payment
        )
        return if (result.success) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.badRequest().body(response)
        }
    }

    /**
     * 결제 취소
     */
    @PostMapping("/{paymentId}/cancel")
    fun cancelPayment(
        paymentId: String,
        @RequestBody request: CancelPaymentRequest
    ): ResponseEntity<PaymentApiResponse> {

        val result = paymentApplicationService.cancelPayment(
            paymentId = paymentId,
            reason = request.reason,
            amount = request.amount
        )

        val response = PaymentApiResponse(
            success = result.success,
            message = result.message,
            payment = result.payment
        )


        return if (result.success) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.badRequest().body(response)
        }
    }

    /**
     * 결제 단건 조회
     */
    @GetMapping("/{paymentId}")
    fun getPayment(
        @PathVariable paymentId: String
    ): ResponseEntity<PaymentApiResponse> {

        val payment = paymentService.getPayment(paymentId)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(
            PaymentApiResponse(
                success = true,
                message = null,
                payment = PaymentDto.from(payment)
            )
        )
    }

    @PostMapping("/webhook")
    fun handleWebhook(
        @RequestBody webhook: PortOneWebhook,
        @RequestHeader("x-portone-signature") signature: String?
    ): ResponseEntity<String> {
        paymentApplicationService.handleWebhook(
            Webhook(
                type = webhook.type,
                timestamp = webhook.timestamp,
                data = WebhookData(
                    paymentId = webhook.data?.paymentId,
                    transactionId = webhook.data?.transactionId,
                    status = webhook.data?.status,
                    totalAmount = webhook.data?.totalAmount
                )
            )
        )

        return ResponseEntity.ok("OK")
    }
}

// Request/Response DTOs
data class PreparePaymentApiRequest(
    val paymentId: String,

    val productType: ProductType,
    val productUuid: UUID,
    val productName: String,

    val amount: Int,

    val buyerEmail: String,
    val buyerName: String,
    val buyerPhone: String
)

data class PreparePaymentApiResponse(
    val success: Boolean,
    val paymentId: String,
    val storeId: String,
    val channelKey: String,
    val orderName: String,
    val amount: Int,
    val currency: Currency
)

data class VerifyPaymentRequest(
    val paymentId: String
)

data class CancelPaymentRequest(
    val reason: String,

    val amount: Int? = null  // null이면 전액 취소
)

data class PaymentApiResponse(
    val success: Boolean,
    val message: String?,
    val payment: PaymentDto?
)

data class PaymentDto(
    val paymentId: String,
    val productUuid: UUID,
    val status: String,
    val amount: Int,
    val cancelledAmount: Int,
    val orderName: String,
    val paidAt: String?,
    val cancelledAt: String?
) {
    companion object {
        fun from(entity: PaymentEntity) = PaymentDto(
            paymentId = entity.paymentId,
            productUuid = entity.productUuid,
            status = entity.status.name,
            amount = entity.amount,
            cancelledAmount = entity.cancelledAmount,
            orderName = entity.productName,
            paidAt = entity.paidAt?.toString(),
            cancelledAt = entity.cancelledAt?.toString()
        )
    }
}

// Extension function
fun PaymentEntity.toDto() = PaymentDto(
    paymentId = this.paymentId,
    productUuid = this.productUuid,
    status = this.status.name,
    amount = this.amount,
    cancelledAmount = this.cancelledAmount,
    orderName = this.productName,
    paidAt = this.paidAt?.toString(),
    cancelledAt = this.cancelledAt?.toString()
)


@JsonIgnoreProperties(ignoreUnknown = true)
data class PortOneWebhook(
    val type: String,
    val timestamp: String? = null,
    val data: PortOneWebhookData? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PortOneWebhookData(
    @JsonProperty("paymentId")
    val paymentId: String? = null,

    @JsonProperty("transactionId")
    val transactionId: String? = null,

    val status: String? = null,

    @JsonProperty("totalAmount")
    val totalAmount: Long? = null
)
