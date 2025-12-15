package beyondeyesight.ui

import beyondeyesight.api.PaymentsApiService
import beyondeyesight.application.PaymentApplicationService
import beyondeyesight.domain.exception.InvalidValueException
import beyondeyesight.domain.model.payment.ProductType
import beyondeyesight.domain.model.payment.Webhook
import beyondeyesight.domain.model.payment.WebhookData
import beyondeyesight.model.PaymentWebhook
import beyondeyesight.model.PreparePaymentRequest
import beyondeyesight.model.PreparePaymentResponse
import beyondeyesight.model.VerifyPaymentRequest
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*

@Service
class PaymentController(
    private val paymentApplicationService: PaymentApplicationService,
) : PaymentsApiService {
    override fun paymentWebhook(paymentWebhook: PaymentWebhook) {
        println("haha")
    }

    override fun preparePayment(
        paymentId: String,
        preparePaymentRequest: PreparePaymentRequest
    ): PreparePaymentResponse {
        return paymentApplicationService.preparePayment(
            paymentId = paymentId,
            productType = ProductType.entries.find { it.name == preparePaymentRequest.productType.name }
                ?: throw InvalidValueException(
                    valueName = "productType",
                    value = preparePaymentRequest.productType,
                    reason = "알 수 없는 상품 타입"
                ),
            productUuid = preparePaymentRequest.productUuid,
            amount = preparePaymentRequest.amount,
            productName = preparePaymentRequest.productName,
            buyerUuid = preparePaymentRequest.buyerUuid,
            buyerEmail = preparePaymentRequest.buyerEmail,
            buyerName = preparePaymentRequest.buyerName,
            buyerPhone = preparePaymentRequest.buyerPhone,
            mapper = { paymentId: String, storeId: String, channelKey: String ->
                PreparePaymentResponse(
                    paymentId = paymentId,
                    storeId = storeId,
                    channelKey = channelKey
                )
            }
        )
    }

    override fun verifyPayment(paymentId: String, verifyPaymentRequest: VerifyPaymentRequest) {
        paymentApplicationService.verifyPayment(paymentId, verifyPaymentRequest.paymentToken, verifyPaymentRequest.txId, verifyPaymentRequest.amount)
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
