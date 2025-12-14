package beyondeyesight.infra.service

import beyondeyesight.config.PortoneProperties
import beyondeyesight.domain.exception.payment.PaymentFailException
import beyondeyesight.domain.model.payment.Currency
import beyondeyesight.domain.model.payment.PaymentCancelResponse
import beyondeyesight.domain.model.payment.PaymentClientConfig
import beyondeyesight.domain.model.payment.PaymentDto
import beyondeyesight.domain.service.payment.PaymentGateway
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient


@Service
class PortoneV2PaymentGateway(
    private val portOneWebClient: WebClient,
    private val properties: PortoneProperties
): PaymentGateway {

    private val logger = LoggerFactory.getLogger(javaClass)
    override fun getPayment(paymentId: String): PaymentDto {
        logger.debug("포트원 결제 조회: paymentId=$paymentId")

        try {
            val response = portOneWebClient.get()
                .uri("/payments/{paymentId}", paymentId)
                .retrieve()
                .bodyToMono(PaymentDto::class.java)
                .block() ?: throw PaymentFailException("Payment response is null")

            logger.info("결제 조회 성공: paymentId=$paymentId, status=${response.status}")
            return response

        } catch (e: Exception) {
            logger.error("결제 조회 실패: paymentId=$paymentId", e)
            throw PaymentFailException("Payment Retrieve Fail: ${e.message}")
        }
    }

    override fun cancelPayment(
        paymentId: String,
        reason: String,
        amount: Int?
    ): PaymentCancelResponse {
        logger.info("결제 취소 요청: paymentId=$paymentId, reason=$reason, amount=$amount")

        val requestBody = mutableMapOf<String, Any>("reason" to reason)
        amount?.let { requestBody["amount"] = it }

        try {
            val response = portOneWebClient.post()
                .uri("/payments/{paymentId}/cancel", paymentId)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(PaymentCancelResponse::class.java)
                .block() ?: throw PaymentFailException("Cancel response is null")

            logger.info("결제 취소 성공: paymentId=$paymentId, cancelledAmount=${response.cancellation?.totalAmount}")
            return response

        } catch (e: Exception) {
            logger.error("결제 취소 실패: paymentId=$paymentId", e)
            throw PaymentFailException("결제 취소 실패: ${e.message}")
        }
    }

    override fun preRegisterPayment(paymentId: String, totalAmount: Int, currency: Currency) {
        logger.debug("결제 사전 등록: paymentId=$paymentId, amount=$totalAmount")

        try {
            portOneWebClient.post()
                .uri("/payments/{paymentId}/pre-register", paymentId)
                .bodyValue(
                    mapOf(
                        "storeId" to properties.storeId,
                        "totalAmount" to totalAmount,
                        "currency" to currency
                    )
                )
                .retrieve()
                .bodyToMono(Any::class.java)
                .block()

            logger.info("결제 사전 등록 완료: paymentId=$paymentId")

        } catch (e: Exception) {
            logger.warn("결제 사전 등록 실패 (계속 진행): paymentId=$paymentId, error=${e.message}")
            // 사전 등록 실패해도 결제는 계속 진행 가능
        }
    }

    override fun getPaymentClientConfig(): PaymentClientConfig {
        return PaymentClientConfig(
            storeId = properties.storeId,
            channelKey = properties.channelKey
        )
    }
}