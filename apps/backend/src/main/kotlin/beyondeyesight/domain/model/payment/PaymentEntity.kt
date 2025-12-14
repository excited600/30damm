package beyondeyesight.domain.model.payment

import beyondeyesight.config.uuidV7
import beyondeyesight.domain.exception.InvalidValueException
import beyondeyesight.domain.exception.payment.PaymentStatusException
import beyondeyesight.domain.model.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

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
    var cancelledAt: LocalDateTime?,

    @Column(length = 500)
    var failReason: String?,
): BaseEntity(uuid) {

    companion object {
        fun ready(
            paymentId: String,
            productType: ProductType,
            productUuid: UUID,
            productName: String,
            amount: Int,
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
                buyerEmail = buyerEmail,
                buyerName = buyerName,
                buyerPhone = buyerPhone,
                transactionId = null,
                cancelledAmount = 0,
                paidAt = null,
                cancelledAt = null,
                failReason = null
            )

        }
    }

    fun pay(transactionId: String, paidAt: LocalDateTime) {
        if (status != Status.PENDING) {
            throw PaymentStatusException.cannotPay(status)
        }
        this.transactionId = transactionId
        this.status = Status.PAID
        this.paidAt = paidAt
    }

    fun fail(reason: String) {
        check(status == Status.PENDING) {
            "PENDING 상태에서만 실패 처리 가능합니다. 현재 상태: $status"
        }

        if (status != Status.PENDING) {
            throw PaymentStatusException.cannotFail(status)
        }
        this.status = Status.FAILED
        this.failReason = reason
    }

    fun cancel(cancelledAt: LocalDateTime) {
        if (status != Status.PAID) {
            throw PaymentStatusException.cannotCancel(status)
        }
        this.status = Status.CANCELLED
        this.cancelledAmount = this.amount
        this.cancelledAt = cancelledAt
    }

    fun cancelPartially(cancelAmount: Int, cancelledAt: LocalDateTime) {
        if (status != Status.PAID) {
            throw PaymentStatusException.cannotCancelPartially(status)
        }

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

        this.cancelledAmount += cancelAmount
        this.cancelledAt = cancelledAt

        this.status = if (cancelledAmount >= amount) {
            Status.CANCELLED
        } else {
            Status.PARTIAL_CANCELLED
        }
    }
    fun getCancellableAmount(): Int = amount - cancelledAmount
}