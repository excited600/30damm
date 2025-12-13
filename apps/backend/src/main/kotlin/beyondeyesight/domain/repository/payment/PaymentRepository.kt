package beyondeyesight.domain.repository.payment

import beyondeyesight.domain.model.payment.PaymentEntity
import beyondeyesight.domain.model.payment.ProductType
import beyondeyesight.domain.model.payment.Status
import java.util.UUID

interface PaymentRepository {
    fun findByPaymentId(paymentId: String): PaymentEntity?

    fun findByPaymentIdForUpdate(paymentId: String): PaymentEntity?

    fun findByProductTypeAndProductId(productType: ProductType, productUuid: UUID): List<PaymentEntity>

    fun existsByPaymentId(paymentId: String): Boolean

    fun findPendingPaymentsBefore(
        status: Status,
        before: java.time.LocalDateTime
    ): List<PaymentEntity>

    fun save(paymentEntity: PaymentEntity): PaymentEntity
}