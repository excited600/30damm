package beyondeyesight.infra.repository.payment

import beyondeyesight.domain.model.payment.PaymentEntity
import beyondeyesight.domain.model.payment.ProductType
import beyondeyesight.domain.model.payment.Status
import beyondeyesight.domain.repository.payment.PaymentRepository
import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderer
import jakarta.persistence.EntityManager
import jakarta.persistence.LockModeType
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
class PaymentRepositoryImpl(
    private val paymentJpaRepository: PaymentJpaRepository,
    private val entityManager: EntityManager,
) : PaymentRepository {

    private val renderer = JpqlRenderer()
    private val context = JpqlRenderContext()

    override fun findByPaymentId(paymentId: String): PaymentEntity? {
        return paymentJpaRepository.findAll {
            select(entity(PaymentEntity::class))
                .from(entity(PaymentEntity::class))
                .where(path(PaymentEntity::paymentId).eq(paymentId))
        }.firstOrNull()
    }

    override fun findByPaymentIdForUpdate(paymentId: String): PaymentEntity? {
        val query = jpql {
            select(entity(PaymentEntity::class))
                .from(entity(PaymentEntity::class))
                .where(path(PaymentEntity::paymentId).eq(paymentId))
        }

        val rendered = renderer.render(
            query = query,
            context = context
        )

        return entityManager.createQuery(rendered.query, PaymentEntity::class.java)
            .apply {
                rendered.params.forEach { (key, value) ->
                    setParameter(key, value)
                }
                lockMode = LockModeType.PESSIMISTIC_WRITE
            }
            .resultList
            .firstOrNull()
    }

    override fun findByProductTypeAndProductId(productType: ProductType, productUuid: UUID): List<PaymentEntity> {
        return paymentJpaRepository.findAll {
            select(entity(PaymentEntity::class))
                .from(entity(PaymentEntity::class))
                .where(
                    and(
                        path(PaymentEntity::productType).eq(productType),
                        path(PaymentEntity::productUuid).eq(productUuid)
                    )
                )
        }.filterNotNull()
    }

    override fun existsByPaymentId(paymentId: String): Boolean {
        return paymentJpaRepository.findAll {
            select(entity(PaymentEntity::class))
                .from(entity(PaymentEntity::class))
                .where(path(PaymentEntity::paymentId).eq(paymentId))
        }.isNotEmpty()
    }

    override fun findPendingPaymentsBefore(
        status: Status,
        before: LocalDateTime
    ): List<PaymentEntity> {
        return paymentJpaRepository.findAll {
            select(entity(PaymentEntity::class))
                .from(entity(PaymentEntity::class))
                .where(
                    and(
                        path(PaymentEntity::status).eq(status),
                        path(PaymentEntity::createdAt).lt(before)
                    )
                )
        }.filterNotNull()
    }

    override fun save(paymentEntity: PaymentEntity): PaymentEntity {
        return paymentJpaRepository.save(paymentEntity)
    }
}