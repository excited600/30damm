package beyondeyesight.domain.service.payment

import beyondeyesight.domain.exception.LockAcquireFailException
import beyondeyesight.domain.exception.ResourceNotFoundException
import beyondeyesight.domain.exception.payment.CannotCancelException
import beyondeyesight.domain.exception.payment.CannotConfirmException
import beyondeyesight.domain.exception.payment.VerificationFailedException
import beyondeyesight.domain.model.payment.Currency
import beyondeyesight.domain.model.payment.PaymentEntity
import beyondeyesight.domain.model.payment.ProductType
import beyondeyesight.domain.model.payment.Status
import beyondeyesight.domain.repository.payment.PaymentRepository
import beyondeyesight.domain.service.LockService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.UUID

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val paymentGateway: PaymentGateway,
    private val paymentSynchronizeService: PaymentSynchronizeService,
    private val lockService: LockService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /* 멱등하게 하고싶어서 lock을 씀. lock을 안쓰면 paymentId unique constraint에 걸려 500 에러가 내려갈 수 있음. 큰 문제가 아닌데 과하게 코딩이 된 것 같기도 하다.*/
    fun preparePayment(
        paymentId: String,
        productType: ProductType,
        productUuid: UUID,
        amount: Int,
        productName: String,
        buyerUuid: UUID,
        buyerEmail: String,
        buyerName: String,
        buyerPhone: String,
    ) {
        val lockToken = lockService.lockWithRetry(
            resourceName = PaymentEntity.RESOURCE_NAME, resourceId = paymentId, expire = Duration.ofSeconds(10),
            waitTimeout = Duration.ofSeconds(20),
            retryInterval = Duration.ofMillis(100)
        ) ?: throw LockAcquireFailException.forResource(
            resourceName = PaymentEntity.RESOURCE_NAME,
            resourceId = paymentId,
            duration = Duration.ofSeconds(20)
        )
        val paymentEntity: PaymentEntity
        try {
            if (paymentRepository.existsByPaymentId(paymentId)) {
                return
            }
            paymentEntity = paymentRepository.save(
                PaymentEntity.ready(
                    paymentId = paymentId,
                    productUuid = productUuid,
                    amount = amount,
                    productName = productName,
                    buyerUuid = buyerUuid,
                    buyerEmail = buyerEmail,
                    buyerName = buyerName,
                    buyerPhone = buyerPhone,
                    productType = productType,
                )
            )
            paymentGateway.preRegisterPayment(paymentId, amount, Currency.KRW)
            logger.info("[3040] Payment 준비 완료. paymentId=$paymentId, productType=${paymentEntity.productType} productUuid=${paymentEntity.productUuid}, amount=${paymentEntity.amount}")
        } finally {
            lockService.unlock(
                resourceName = PaymentEntity.RESOURCE_NAME,
                resourceId = paymentId,
                token = lockToken
            )
        }
    }

    fun cancelPayment(paymentId: String, reason: String, amount: Int) {
        try {
            val pgPayment = paymentGateway.getPayment(paymentId)
            if (pgPayment.status != Status.PAID) {

                logger.error("[3040] 결제 상태가 취소 가능한 상태가 아님: ${pgPayment.status}")
                throw CannotCancelException.invalidPaymentStatus(
                    paymentId = paymentId,
                    status = pgPayment.status
                )
            }
            paymentGateway.cancelPayment(
                paymentId = paymentId,
                reason = reason,
                amount = amount
            )
            logger.info("[3040] 결제 취소 성공. paymentId: $paymentId, amount: $amount, reason: $reason")
        } finally {
            paymentSynchronizeService.synchronize(paymentId = paymentId)
        }
    }

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
            paymentSynchronizeService.synchronize(paymentId)
        }
    }
}