package beyondeyesight.domain.service.payment

import beyondeyesight.domain.exception.InvalidValueException
import beyondeyesight.domain.exception.ResourceNotFoundException
import beyondeyesight.domain.exception.payment.InvalidOperationException
import beyondeyesight.domain.exception.payment.PaymentFailException
import beyondeyesight.domain.model.payment.Currency
import beyondeyesight.domain.model.payment.Payment
import beyondeyesight.domain.model.payment.PaymentEntity
import beyondeyesight.domain.model.payment.ProductType
import beyondeyesight.domain.model.payment.Status
import beyondeyesight.domain.repository.payment.PaymentRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class PaymentStateService(
    private val paymentRepository: PaymentRepository,
    private val paymentGateway: PaymentGateway,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun cancelPayment(
        paymentId: String,
        reason: String,
        amount: Int
    ) {
        logger.info("Payment Cancel Request: paymentId=$paymentId, reason=$reason, amount=$amount")

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
    fun synchronize(
        paymentEntity: PaymentEntity, paymentDto: Payment
    ): PaymentEntity {
        paymentEntity.synchronize(paymentDto)
        return paymentRepository.save(paymentEntity)
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun cancel(payment: PaymentEntity, cancelledAt: LocalDateTime?, reason: String) {
        payment.cancel(
            cancelledAt = cancelledAt ?: LocalDateTime.now(), reason = reason,
            cancelAmount = payment.getCancellableAmount()
        )
        paymentRepository.save(payment)
    }

    fun preparePayment(
        paymentId: String,
        productType: ProductType,
        productUuid: UUID,
        amount: Int,
        productName: String,
        buyerEmail: String,
        buyerName: String,
        buyerPhone: String,
    ) {
        if (paymentRepository.existsByPaymentId(paymentId)) {
            throw InvalidValueException(
                valueName = "paymentId",
                value = paymentId,
                reason = "Already exists"
            )
        }

        val paymentEntity = paymentRepository.save(
            PaymentEntity.ready(
                paymentId = paymentId,
                productUuid = productUuid,
                amount = amount,
                productName = productName,
                buyerEmail = buyerEmail,
                buyerName = buyerName,
                buyerPhone = buyerPhone,
                productType = productType,
            )
        )

        try {
            paymentGateway.preRegisterPayment(paymentId, amount, Currency.KRW)
        } catch (e: Exception) {
            logger.warn("Pre Register by PaymentGateway failed. paymentId: $paymentId, amount: $amount, ${e.message}")
        }

        logger.info("[3040] Payment 준비 완료. paymentId=$paymentId, productType=${paymentEntity.productType} productUuid=${paymentEntity.productUuid}, amount=${paymentEntity.amount}")
    }

    data class PreparePaymentResponse(
        val paymentId: String,
        val storeId: String,
        val channelKey: String,
    )
}