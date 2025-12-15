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
        val paymentDto = paymentGateway.getPayment(paymentId)
        // 검증: 금액 일치 여부 (위변조 방지)
        if (paymentDto.amount.total != paymentEntity.amount) {
            logger.error(
                "[3040] 서버 금액과 pg 금액이 다름. server=${paymentEntity.amount}, pg=${paymentDto.amount.total}, paymentId=$paymentId"
            )
            throw VerificationFailedException.invalidAmount(
                serverAmount = paymentEntity.amount,
                pgAmount = paymentDto.amount.total
            )
        }
        // 동기화
        val synchronized = paymentStateService.synchronize(paymentEntity = paymentEntity, paymentDto = paymentDto)
        when (synchronized.status) {
            Status.READY -> paymentGateway.confirm(
                paymentId = paymentId,
                paymentToken = paymentToken,
                txId = txId,
                amount = amount
            )

            Status.PAID -> return
            Status.PAY_PENDING, Status.VIRTUAL_ACCOUNT_ISSUED, Status.FAILED, Status.CANCELLED, Status.PARTIAL_CANCELLED ->
                throw CannotConfirmException.invalidStatus(synchronized.status)
        }
    }

    private fun onVerificationFailed(
        paymentEntity: PaymentEntity,
        paymentDto: Payment,
        paymentId: String
    ): Nothing {
        logger.error(
            "[3040] 서버 금액과 pg 금액이 다름. server=${paymentEntity.amount}, pg=${paymentDto.amount.total}, paymentId=$paymentId"
        )
        try {
            val cancelPaymentResponse =
                paymentGateway.cancelPayment(paymentId, "Auto cancel due to amount mismatch", null)
            paymentStateService.cancel(
                payment = paymentEntity,
                cancelledAt = cancelPaymentResponse.cancellation?.cancelledAt,
                reason = "결제 요청한 금액과 결제된 금액이 상이: 결제 요청 금액=${paymentEntity.amount}, 결제된 금액=${paymentDto.amount}"
            )
            logger.info("Auto cancel duo to amount mismatch complete : paymentId=$paymentId")
        } catch (e: Exception) {
            logger.error("Auto cancel failed. manual cancel is needed : paymentId=$paymentId", e)
            //TODO: 재시도 로직 등. 이건 크리티컬하기 때문에 어떻게 처리할지 더 고민.
        }
        throw VerificationFailedException.invalidAmount(
            serverAmount = paymentEntity.amount,
            pgAmount = paymentDto.amount.total
        )
    }
}