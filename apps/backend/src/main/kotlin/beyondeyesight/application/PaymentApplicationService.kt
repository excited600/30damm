package beyondeyesight.application

import beyondeyesight.domain.exception.InvalidValueException
import beyondeyesight.domain.model.payment.ProductType
import beyondeyesight.domain.model.payment.Webhook
import beyondeyesight.domain.service.payment.PaymentStateService
import beyondeyesight.domain.service.payment.PaymentVerificationService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class PaymentApplicationService(
    private val paymentVerificationService: PaymentVerificationService,
    private val paymentStateService: PaymentStateService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun handleWebhook(webhook: Webhook) {
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
                        verifyPayment(paymentId)
                        logger.info("웹훅 결제 검증 결과: paymentId=$paymentId, success=${true}")
                    }
                }

                "Transaction.Cancelled" -> {
                    // 포트원 콘솔에서 직접 취소한 경우
                    logger.info("취소 웹훅 수신: paymentId=${webhook.data?.paymentId}")
                    cancelPayment(
                        paymentId = webhook.data?.paymentId?: throw InvalidValueException("paymentId", Unit, "취소 웹훅에 paymentId 누락"),
                        reason = "이거 웹훅 요청 어떻게 오는지 보고, 그에 맞춰서 대응", //TODO
                        amount = webhook.data.totalAmount
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

    @Transactional
    fun preparePayment(
        paymentId: String,
        productType: ProductType,
        productUuid: UUID,
        amount: Int,
        productName: String,
        buyerEmail: String,
        buyerName: String,
        buyerPhone: String
    ): PaymentStateService.PreparePaymentResponse {
        return paymentStateService.preparePayment(
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

    @Transactional
    fun verifyPayment(paymentId: String){
        paymentVerificationService.verifyPayment(paymentId)
    }

    @Transactional
    fun cancelPayment(paymentId: String, reason: String, amount: Int) {
        paymentStateService.cancelPayment(paymentId, reason, amount)
    }

}