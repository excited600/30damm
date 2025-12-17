package beyondeyesight.domain.service.payment

import beyondeyesight.domain.exception.ResourceNotFoundException
import beyondeyesight.domain.model.payment.PaymentEntity
import beyondeyesight.domain.repository.payment.PaymentRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentSynchronizeService(
    private val paymentRepository: PaymentRepository,
    private val paymentGateway: PaymentGateway,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun synchronize(paymentId: String) {
        val paymentEntity = paymentRepository.findByPaymentId(paymentId)?: throw ResourceNotFoundException.byField(
            resourceName = PaymentEntity.RESOURCE_NAME,
            fieldName = "paymentId",
            fieldValue = paymentId
        ).also { logger.error("[3040] 동기화 실패 - paymentId: $paymentId 에 해당하는 엔티티가 없습니다.") }

        val pgPayment = paymentGateway.getPayment(paymentId)
        paymentEntity.synchronize(pgPayment)
        paymentRepository.save(paymentEntity)
    }
}