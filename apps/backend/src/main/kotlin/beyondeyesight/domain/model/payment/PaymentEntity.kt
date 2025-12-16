package beyondeyesight.domain.model.payment

import beyondeyesight.config.uuidV7
import beyondeyesight.domain.exception.InvalidValueException
import beyondeyesight.domain.model.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
    name = "payments",
)
class PaymentEntity(

    uuid: UUID,

    /**
     * 우리 시스템의 주문번호 = 포트원의 paymentId
     * V2에서는 merchant_uid 대신 paymentId를 사용
     * 결제 요청 시 프론트에서 생성해서 전달
     */
    @Column(nullable = false, unique = true, length = 100)
    val paymentId: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val productType: ProductType,

    @Column(nullable = false)
    val productUuid: UUID,

    /**
     * 결제 금액 (원 단위)
     */
    @Column(nullable = false)
    val amount: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: Status,

    @Column(nullable = false, length = 200)
    val productName: String,

    @Column(name = "buyer_uuid", nullable = false)
    val buyerUuid: UUID,

    @Column(length = 100)
    val buyerEmail: String?,

    @Column(length = 50)
    val buyerName: String?,

    @Column(length = 20)
    val buyerPhone: String?,

    /**
     * 포트원 트랜잭션 ID (결제 완료 후 설정)
     */
    @Column(length = 100)
    var transactionId: String?,

    /**
     * 취소된 총 금액
     */
    @Column(nullable = false)
    var cancelledAmount: Int = 0,

    @Column(nullable = true)
    var paidAt: LocalDateTime?,

    @Column(nullable = true)
    var failedAt: LocalDateTime?,

    @Column(nullable = true)
    var cancelledAt: LocalDateTime?,

    @Column(length = 500)
    var cancelReason: String?,
    @Column(length = 500)
    var failReason: String?,
) : BaseEntity(uuid) {

    companion object {
        fun ready(
            paymentId: String,
            productType: ProductType,
            productUuid: UUID,
            productName: String,
            amount: Int,
            buyerUuid: UUID,
            buyerEmail: String,
            buyerName: String,
            buyerPhone: String,
        ): PaymentEntity {
            return PaymentEntity(
                uuid = uuidV7(),
                paymentId = paymentId,
                productType = productType,
                productUuid = productUuid,
                amount = amount,
                status = Status.READY,
                productName = productName,
                buyerUuid = buyerUuid,
                buyerEmail = buyerEmail,
                buyerName = buyerName,
                buyerPhone = buyerPhone,
                transactionId = null,
                cancelledAmount = 0,
                paidAt = null,
                cancelledAt = null,
                failedAt = null,
                cancelReason = null,
                failReason = null
            )
        }

        const val RESOURCE_NAME = "payments"
    }

    fun pay(paidAt: LocalDateTime) {
        this.status = Status.PAID
        this.paidAt = paidAt
    }

    fun synchronize(payment: Payment) {
        //TODO: 트랜잭션 id 여러개로. 부분취소 지원하기 위해.
        this.transactionId = payment.transactionId
        when (payment) {
            is PaymentCancelled -> {
                this.cancel(
                    cancelAmount = payment.amount.cancelled,
                    cancelledAt = payment.cancelledAt,
                    reason = null // TODO: 이유 넣어야할거같은데...
                )
            }

            is PaymentFailed -> {
                this.fail(
                    reason = payment.failure.reason,
                    failedAt = payment.failedAt
                )
            }

            is PaymentPaid -> {
                this.pay(payment.paidAt)
            }

            is PaymentPartialCancelled -> {
                this.cancelPartially(
                    cancelAmount = payment.amount.cancelled,
                    cancelledAt = payment.cancelledAt,
                    reason = null
                )
            }

            is PaymentPayPending -> {
                this.payPending()
            }

            is PaymentReady -> {
                this.ready()
            }

            is PaymentVirtualAccountIssued -> {
                this.virtualAccountIssued()
            }
        }
    }

    fun ready() {
        this.status = Status.READY
    }

    fun virtualAccountIssued() {
        this.status = Status.VIRTUAL_ACCOUNT_ISSUED
    }

    fun payPending() {
        this.status = Status.PAY_PENDING
    }

    fun fail(reason: String, failedAt: LocalDateTime) {
        this.status = Status.FAILED
        this.failedAt = failedAt
        this.failReason = reason
    }

    fun cancel(cancelAmount: Int, cancelledAt: LocalDateTime, reason: String?) {
        if (cancelAmount <= 0) {
            throw InvalidValueException(
                valueName = "cancelAmount",
                value = cancelAmount,
                reason = "cancel amount must be greater than 0"
            )
        }
        this.status = Status.CANCELLED
        this.cancelledAmount = cancelAmount
        this.cancelledAt = cancelledAt
        this.cancelReason = reason
    }

    fun cancelPartially(cancelAmount: Int, cancelledAt: LocalDateTime, reason: String?) {
        if (cancelAmount <= 0) {
            throw InvalidValueException(
                valueName = "cancelAmount",
                value = cancelAmount,
                reason = "cancel amount must be greater than 0"
            )
        }
        if (amount < cancelledAmount + cancelAmount) {
            throw InvalidValueException(
                valueName = "cancelAmount",
                value = cancelAmount,
                reason = "cancel amount exceeds the remaining amount. requested: $cancelAmount, cancellable: ${amount - cancelledAmount}"
            )
        }

        this.status = Status.PARTIAL_CANCELLED
        this.cancelledAmount += cancelAmount
        this.cancelledAt = cancelledAt
        this.cancelReason = reason
    }

    fun getCancellableAmount(): Int = amount - cancelledAmount

}