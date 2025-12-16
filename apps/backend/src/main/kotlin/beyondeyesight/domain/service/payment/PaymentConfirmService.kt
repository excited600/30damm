package beyondeyesight.domain.service.payment

import beyondeyesight.domain.exception.ResourceNotFoundException
import beyondeyesight.domain.exception.payment.CannotConfirmException
import beyondeyesight.domain.exception.payment.VerificationFailedException
import beyondeyesight.domain.model.payment.Payment
import beyondeyesight.domain.model.payment.PaymentEntity
import beyondeyesight.domain.model.payment.Status
import beyondeyesight.domain.repository.payment.PaymentRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PaymentConfirmService(
    private val paymentRepository: PaymentRepository,
    private val paymentGateway: PaymentGateway,
    private val paymentStateService: PaymentStateService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun confirmPayment(paymentId: String, paymentToken: String, txId: String, amount: Int) {
        logger.info("[3040] 결제 컨펌 시작. paymentId=$paymentId")

        val paymentEntity = paymentRepository.findByPaymentId(paymentId)
            ?: throw ResourceNotFoundException.byField(
                resourceName = "Payment",
                fieldName = "paymentId",
                fieldValue = paymentId
            )
        val pgPayment = paymentGateway.getPayment(paymentId)
        // 검증: 금액 일치 여부 (위변조 방지)
        if (pgPayment.amount.total != paymentEntity.amount) {
            logger.error(
                "[3040] 서버 금액과 pg 금액이 다름. server=${paymentEntity.amount}, pg=${pgPayment.amount.total}, paymentId=$paymentId"
            )
            throw VerificationFailedException.invalidAmount(
                serverAmount = paymentEntity.amount,
                pgAmount = pgPayment.amount.total
            )
        }
        try {
            when (pgPayment.status) {
                Status.READY -> {
                    paymentGateway.confirm(
                        paymentId = paymentId,
                        paymentToken = paymentToken,
                        txId = txId,
                        amount = amount
                    )
                }

                Status.PAID -> return
                Status.PAY_PENDING, Status.VIRTUAL_ACCOUNT_ISSUED, Status.FAILED, Status.CANCELLED, Status.PARTIAL_CANCELLED ->
                    throw CannotConfirmException.invalidStatus(pgPayment.status)
            }
        } finally {
            paymentStateService.synchronize(paymentId)
        }
    }
}