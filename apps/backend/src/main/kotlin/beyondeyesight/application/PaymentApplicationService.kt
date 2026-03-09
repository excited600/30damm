package beyondeyesight.application

import beyondeyesight.domain.model.payment.ProductType
import beyondeyesight.domain.model.payment.Webhook
import beyondeyesight.domain.model.payment.WebhookType
import beyondeyesight.domain.repository.payment.PaymentRepository
import beyondeyesight.domain.service.gathering.GatheringService
import beyondeyesight.domain.service.payment.PaymentGateway
import beyondeyesight.domain.service.payment.PaymentService
import beyondeyesight.domain.service.payment.PaymentSynchronizeService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class PaymentApplicationService(
    private val paymentSynchronizeService: PaymentSynchronizeService,
    private val paymentService: PaymentService,
    private val paymentGateway: PaymentGateway,
    private val paymentRepository: PaymentRepository,
    private val gatheringService: GatheringService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun handleWebhook(webhook: Webhook) {
        logger.info(
            "웹훅 수신: type=${webhook.type}, paymentId=${webhook.data.paymentId}, " +
                    "transactionId=${webhook.data.transactionId}, timestamp=${webhook.timestamp}"
        )

        // TODO: 시그니처 검증 (프로덕션에서 필수)
        // verifyWebhookSignature(signature, rawBody)

        val paymentEntity = paymentRepository.findByPaymentId(webhook.data.paymentId) ?: run {
            logger.error("[30damm] 웹훅 수신된 paymentId=${webhook.data.paymentId}에 해당하는 결제 건을 찾을 수 없음. 무시 처리.")
            return
        }

        when (webhook.type) {
            WebhookType.TransactionPeriodReady -> {
                logger.info("[30damm] 결제 준비 웹훅 수신: paymentId=${paymentEntity.paymentId}. 무시합니다.")
                return
            }

            WebhookType.TransactionPeriodPaid -> {
                when (paymentEntity.productType) {
                    ProductType.GATHERING -> gatheringService.handlePaidWebhook(paymentEntity)
                }
                return
            }

            WebhookType.TransactionPeriodVirtualAccountIssued -> {
                logger.info("[30damm] 가상결제 발급 웹훅 수신: paymentId=${paymentEntity.paymentId}. 무시합니다.")
                return
            }

            WebhookType.TransactionPeriodPartialCancelled -> {
                when (paymentEntity.productType) {
                    ProductType.GATHERING -> gatheringService.handlePartialCancelledWebhook(paymentEntity)
                }
                return
            }

            WebhookType.TransactionPeriodCancelled -> {
                when (paymentEntity.productType) {
                    ProductType.GATHERING -> gatheringService.handleCancelledWebhook(paymentEntity)
                }
                return
            }

            WebhookType.TransactionPeriodFailed -> {
                when (paymentEntity.productType) {
                    ProductType.GATHERING -> gatheringService.handleFailedWebhook(paymentEntity)
                }
                return
            }
            WebhookType.TransactionPeriodPayPending -> {
                logger.info("[30damm] 결제 대기 웹훅 수신: paymentId=${paymentEntity.paymentId}. 무시합니다.")
                return
            }
            WebhookType.TransactionPeriodCancelPending -> {
                logger.info("[30damm] 취소 대기 웹훅 수신: paymentId=${paymentEntity.paymentId}. 무시합니다.")
                return
            }
            WebhookType.TransactionPeriodDisputeCreated -> {
                logger.info("[30damm] DisputeCreated 웹훅 수신: paymentId=${paymentEntity.paymentId}. 무시합니다.")
                return
            }
            WebhookType.TransactionPeriodDisputeResolved -> {
                logger.info("[30damm] DisputeResolved 웹훅 수신: paymentId=${paymentEntity.paymentId}. 무시합니다.")
                return
            }
        }
    }

    @Transactional
    fun <R> preparePayment(
        paymentId: String,
        productType: ProductType,
        productUuid: UUID,
        amount: Int,
        productName: String,
        buyerUuid: UUID,
        buyerEmail: String,
        buyerName: String,
        buyerPhone: String,
        mapper: (String, String, String) -> R
    ): R {
        paymentService.preparePayment(
            paymentId = paymentId,
            productType = productType,
            productUuid = productUuid,
            amount = amount,
            productName = productName,
            buyerUuid = buyerUuid,
            buyerEmail = buyerEmail,
            buyerName = buyerName,
            buyerPhone = buyerPhone
        )

        val paymentConfig = paymentGateway.getPaymentClientConfig()

        return mapper.invoke(paymentId, paymentConfig.storeId, paymentConfig.channelKey)
    }
}