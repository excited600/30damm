package beyondeyesight.domain.service.payment

import beyondeyesight.domain.exception.InvalidValueException
import beyondeyesight.domain.exception.LockAcquireFailException
import beyondeyesight.domain.exception.ResourceNotFoundException
import beyondeyesight.domain.exception.payment.InvalidOperationException
import beyondeyesight.domain.model.payment.Currency
import beyondeyesight.domain.model.payment.PaymentEntity
import beyondeyesight.domain.model.payment.ProductType
import beyondeyesight.domain.model.payment.Status
import beyondeyesight.domain.repository.payment.PaymentRepository
import beyondeyesight.domain.service.LockService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@Service
class PaymentStateService(
    private val paymentRepository: PaymentRepository,
    private val paymentGateway: PaymentGateway,
    private val lockService: LockService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun cancelPayment(
        paymentId: String,
        reason: String,
        amount: Int
    ) {
        logger.info("결제 취소 요청: paymentId=$paymentId, reason=$reason, amount=$amount")

        val payment = paymentRepository.findByPaymentIdForUpdate(paymentId)
            ?: throw ResourceNotFoundException.byField(
                resourceName = "Payment",
                fieldName = "paymentId",
                fieldValue = paymentId
            )

        if (payment.status != Status.PAID && payment.status != Status.PARTIAL_CANCELLED) {
            throw InvalidOperationException.cannotCancel("Payment의 상태가 취소 가능한 상태가 아님: ${payment.status}")
        }

        if (amount > payment.getCancellableAmount()) {
            throw InvalidValueException(
                valueName = "amount",
                value = amount,
                reason = "취소 요청 금액이 취소 가능 금액(${payment.getCancellableAmount()})보다 큼"
            )
        }

        paymentGateway.cancelPayment(paymentId, reason, amount)

        if (amount == payment.getCancellableAmount()) {
            payment.cancel(
                cancelAmount = amount,
                cancelledAt = LocalDateTime.now(),
                reason = "고객 요청 전체 취소"
            )
        } else {
            payment.cancelPartially(cancelAmount = amount, cancelledAt = LocalDateTime.now(), reason = "고객 요청 부분 취소")
        }

        logger.info("결제 취소 완료: paymentId=$paymentId, cancelledAmount=$amount")
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun synchronize(paymentId: String) {
        val paymentEntity = paymentRepository.findByPaymentId(paymentId)?: run {
            logger.error("[3040] 동기화 실패 - paymentId: $paymentId 에 해당하는 엔티티가 없습니다.")
            throw ResourceNotFoundException.byField(
                resourceName = PaymentEntity.RESOURCE_NAME,
                fieldName = "paymentId",
                fieldValue = paymentId
            )
        }
        val pgPayment = paymentGateway.getPayment(paymentId)
        paymentEntity.synchronize(pgPayment)
        paymentRepository.save(paymentEntity)
    }


    // Toodo: 이거 필요없을듯 웹훅 보고 얘기하자..
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun cancel(payment: PaymentEntity, cancelledAt: LocalDateTime?, reason: String) {
        payment.cancel(
            cancelledAt = cancelledAt ?: LocalDateTime.now(), reason = reason,
            cancelAmount = payment.getCancellableAmount()
        )
        paymentRepository.save(payment)
    }

    /* 멱등하게 하고싶어서 lock을 씀. lock을 안쓰면 paymentId unique constraint에 걸려 500 에러가 내려갈 수 있음. 그런데 큰 문제가 아닌데 과하게 코딩이 된 것 같기도 하다.*/
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
}