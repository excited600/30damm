package beyondeyesight.application

import beyondeyesight.domain.exception.InvalidValueException
import beyondeyesight.domain.model.payment.ProductType
import beyondeyesight.domain.model.payment.Webhook
import beyondeyesight.domain.service.payment.PaymentService
import beyondeyesight.domain.service.payment.PaymentService.VerifyPaymentResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PaymentApplicationService(
    private val paymentService: PaymentService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun handleWebhook(webhook: Webhook) {
        logger.info(
            "웹훅 수신: type=${webhook.type}, paymentId=${webhook.data?.paymentId}, " +
                    "status=${webhook.data?.status}"
        )

        // TODO: 시그니처 검증 (프로덕션에서 필수)
        // verifyWebhookSignature(signature, rawBody)

        try {
            when (webhook.type) {
                "Transaction.Paid" -> {
                    // 결제 완료 웹훅
                    webhook.data?.paymentId?.let { paymentId ->
                        val result = verifyPayment(paymentId)
                        logger.info("웹훅 결제 검증 결과: paymentId=$paymentId, success=${result.success}")
                    }
                }

                "Transaction.Cancelled" -> {
                    // 포트원 콘솔에서 직접 취소한 경우
                    logger.info("취소 웹훅 수신: paymentId=${webhook.data?.paymentId}")
                    cancelPayment(
                        paymentId = webhook.data?.paymentId?: throw InvalidValueException("paymentId", Unit, "취소 웹훅에 paymentId 누락"),
                        reason = "이거 웹훅 요청 어떻게 오는지 보고, 그에 맞춰서 대응", //TODO
                        amount = webhook.data.totalAmount?.toInt()
                    )
                    // 필요 시 DB 동기화 처리
                }

                "Transaction.Failed" -> {
                    logger.info("결제 실패 웹훅: paymentId=${webhook.data?.paymentId}")
                    // TODO
                }

                "Transaction.VirtualAccountIssued" -> {
                    logger.info("가상계좌 발급 웹훅: paymentId=${webhook.data?.paymentId}")
                    // TODO
                }

                else -> {
                    logger.debug("처리하지 않는 웹훅 타입: ${webhook.type}")
                }
            }
        } catch (e: Exception) {
            logger.error("웹훅 처리 실패: ${webhook.type}", e)
            // 웹훅 처리 실패해도 200 반환 (재시도 루프 방지)
            // 대신 로그/알림으로 모니터링
        }
    }

    suspend fun preparePayment(
        paymentId: String,
        productType: ProductType,
        productUuid: UUID,
        amount: Int,
        productName: String,
        buyerEmail: String,
        buyerName: String,
        buyerPhone: String
    ): PaymentService.PreparePaymentResponse {
        return paymentService.preparePayment(
            paymentId = paymentId,
            productType = productType,
            productUuid = productUuid,
            amount = amount,
            productName = productName,
            buyerEmail = buyerEmail,
            buyerName = buyerName,
            buyerPhone = buyerPhone
        )
    }

    suspend fun verifyPayment(paymentId: String): VerifyPaymentResponse {
        return paymentService.verifyPayment(paymentId)
    }

    suspend fun cancelPayment(paymentId: String, reason: String, amount: Int?): PaymentService.CancelPaymentResponse {
        return paymentService.cancelPayment(paymentId, reason, amount)
    }

}