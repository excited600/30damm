package beyondeyesight.ui

import beyondeyesight.api.PaymentsApiService
import beyondeyesight.application.PaymentApplicationService
import beyondeyesight.domain.exception.InvalidValueException
import beyondeyesight.domain.model.payment.ProductType
import beyondeyesight.domain.model.payment.Webhook
import beyondeyesight.domain.model.payment.WebhookData
import beyondeyesight.domain.model.payment.WebhookType
import beyondeyesight.model.PaymentWebhook
import beyondeyesight.model.PreparePaymentRequest
import beyondeyesight.model.PreparePaymentResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PaymentController(
    private val paymentApplicationService: PaymentApplicationService,
) : PaymentsApiService {
    val logger: Logger = LoggerFactory.getLogger(javaClass)

    override fun paymentWebhook(paymentWebhook: PaymentWebhook) {
        val webhookType = WebhookType.entries.find { it.name == paymentWebhook.type.name } ?: run {
            logger.error("[3040] 알 수 없는 웹훅 타입 수신: type=${paymentWebhook.type.name}. 무시 처리.")
            return
        }
        paymentApplicationService.handleWebhook(
            webhook = Webhook(
                type = webhookType,
                timestamp = paymentWebhook.timestamp,
                data = WebhookData(
                    paymentId = paymentWebhook.data.paymentId,
                    transactionId = paymentWebhook.data.transactionId,
                    storeId = paymentWebhook.data.storeId,
                    cancellationId = paymentWebhook.data.cancellationId
                )
            )
        )
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
}
