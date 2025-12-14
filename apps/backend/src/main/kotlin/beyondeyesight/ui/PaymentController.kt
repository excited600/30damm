package beyondeyesight.ui

import beyondeyesight.application.PaymentApplicationService
import beyondeyesight.domain.model.payment.*
import beyondeyesight.domain.service.payment.PaymentVerificationService
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/payments")
class PaymentController(
    private val paymentVerificationService: PaymentVerificationService,
    private val paymentApplicationService: PaymentApplicationService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

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
                paymentId = result.paymentId,
                storeId = result.storeId,
                channelKey = result.channelKey,
            )
        )
    }

    /**
     * 결제 검증 - 프론트에서 결제 완료 후 호출
     */
    @PostMapping("/verify")
    fun verifyPayment(
        @RequestBody request: VerifyPaymentRequest
    ): ResponseEntity<Unit> {
        val result = paymentApplicationService.verifyPayment(request.paymentId)
        return ResponseEntity.ok(Unit);
    }

    /**
     * 결제 취소
     */
    @PostMapping("/{paymentId}/cancel")
    fun cancelPayment(
        paymentId: String,
        @RequestBody request: CancelPaymentRequest
    ): ResponseEntity<Unit> {

        val result = paymentApplicationService.cancelPayment(
            paymentId = paymentId,
            reason = request.reason,
            amount = request.amount
        )

        return ResponseEntity.ok(Unit)
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
                    totalAmount = webhook.data?.totalAmount ?: 0 // TODO: 이거 0 값 확인해봐야.
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
    val paymentId: String,
    val storeId: String,
    val channelKey: String,
)

data class VerifyPaymentRequest(
    val paymentId: String
)

data class CancelPaymentRequest(
    val reason: String,

    val amount: Int
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
    val totalAmount: Int? = null
)
