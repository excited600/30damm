package beyondeyesight.domain.service.payment

import beyondeyesight.config.uuidV7
import beyondeyesight.domain.exception.InvalidValueException
import beyondeyesight.domain.exception.payment.PaymentFailException
import beyondeyesight.domain.model.payment.Currency
import beyondeyesight.domain.model.payment.PaymentCancelResult
import beyondeyesight.domain.model.payment.PaymentEntity
import beyondeyesight.domain.model.payment.PaymentVerifyResult
import beyondeyesight.domain.model.payment.ProductType
import beyondeyesight.domain.model.payment.Status
import beyondeyesight.domain.repository.payment.PaymentRepository
import beyondeyesight.ui.PaymentDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val paymentGateway: PaymentGateway,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun preparePayment(
        paymentId: String,
        productType: ProductType,
        productUuid: UUID,
        amount: Int,
        productName: String,
        buyerEmail: String,
        buyerName: String,
        buyerPhone: String,
    ): PreparePaymentResponse {
        if (paymentRepository.existsByPaymentId(paymentId)) {
            throw InvalidValueException(
                valueName = "paymentId",
                value = paymentId,
                reason = "Already exists"
            )
        }

        val paymentEntity = paymentRepository.save(
            PaymentEntity.pending(
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

        logger.info("Payment is prepared. paymentId=$paymentId, productType=${paymentEntity.productType} productUuid=${paymentEntity.productUuid}, amount=${paymentEntity.amount}")

        return PreparePaymentResponse(
            paymentId = paymentId,
            storeId = paymentGateway.getPaymentClientConfig().storeId,
            channelKey = paymentGateway.getPaymentClientConfig().channelKey,
            productName = productName,
            amount = paymentEntity.amount,
            currency = Currency.KRW
        )
    }

    suspend fun failPayment(paymentId: String) {

    }

    suspend fun verifyPayment(paymentId: String): VerifyPaymentResponse {
        logger.info("Payment Verification started. paymentId=$paymentId")

        // 비관적 락으로 조회 (동시 요청 대비) // TODO: 락테이블로 바꾸기. 어떤 동시성 문제인지 정확히 알기
        val payment = paymentRepository.findByPaymentIdForUpdate(paymentId)
            ?: run {
                logger.error("Payment not found: paymentId=$paymentId")
                return VerifyPaymentResponse(
                    success = false,
                    message = "Payment not found",
                    payment = null
                )
            }

        // 이미 처리된 결제인지 확인
        if (payment.status != Status.PENDING) {
            logger.warn("Already processed: paymentId=$paymentId, status=${payment.status}")
            return if (payment.status == Status.PAID) {
                VerifyPaymentResponse(
                    success = true,
                    message = "Payment already completed",
                    payment = PaymentDto.from(payment)
                )
            } else {
                VerifyPaymentResponse(
                    success = false,
                    message = "Payment already processed: ${payment.status}",
                    payment = null
                )
            }
        }

        // pg에서 실제 결제 정보 조회
        val paymentDto = try {
            paymentGateway.getPayment(paymentId)
        } catch (e: PaymentFailException) {
            logger.error("Payment Gateway get failed: paymentId=$paymentId", e)
            return VerifyPaymentResponse(
                success = false,
                message = "Payment Gateway get failed: ${e.message}",
                payment = null
            )
        }

        // 검증: 금액 일치 여부 (위변조 방지)
        val paidAmount = paymentDto.amount?.total ?: 0
        if (paidAmount != payment.amount) {
            logger.error(
                "Amount is not same. expected=${payment.amount}, actual=$paidAmount, paymentId=$paymentId"
            )
            try {
                paymentGateway.cancelPayment(paymentId, "Auto cancel due to amount mismatch", null)
                logger.info("Auto cancel duo to amount mismatch complete : paymentId=$paymentId")
            } catch (e: Exception) {
                logger.error("Auto cancel failed. manual cancel is needed : paymentId=$paymentId", e)
            }

            val reason = "Requested and actual amount mismatch: 요청=${payment.amount}, 실제=$paidAmount"
            payment.fail(reason)
            return VerifyPaymentResponse(
                success = false,
                message = reason,
                payment = null
            )
        }

        val status = Status.entries.find { it.name == paymentDto.status }
        return status?.let {
            when (it) {
                Status.READY, Status.VIRTUAL_ACCOUNT_ISSUED -> {
                    // 가상계좌 발급됨 - 입금 대기 상태
                    logger.info("가상계좌 발급 완료, 입금 대기: paymentId=$paymentId")
                    VerifyPaymentResponse(
                        success = true,
                        message = "가상계좌 발급 완료, 입금 대기 중",
                        payment = PaymentDto.from(payment)
                    )
                }

                Status.PAID -> {
                    val paidAt = parseDateTime(paymentDto.paidAt)
                    payment.pay(
                        transactionId = paymentDto.transactionId ?: paymentDto.id, //TODO: 이거 이렇게 해도 되는지...
                        paidAt = paidAt
                    )
                    logger.info("결제 검증 성공: paymentId=$paymentId, amount=${payment.amount}")
                    VerifyPaymentResponse(
                        success = true,
                        message = "Payment is completed",
                        payment = PaymentDto.from(payment)
                    )
                }

                Status.FAILED -> {
                    val reason = paymentDto.failureReason ?: "결제 실패"
                    payment.fail(reason)
                    logger.warn("결제 실패: paymentId=$paymentId, reason=$reason")
                    VerifyPaymentResponse(
                        success = false,
                        message = "Payment failed: $reason",
                        payment = null
                    )

                }

                Status.CANCELLED -> {
                    val cancelledAt = parseDateTime(paymentDto.cancelledAt)
                    payment.cancel(cancelledAt)
                    PaymentVerifyResult.failure("Payment is cancelled", paymentId)
                    VerifyPaymentResponse(
                        success = false,
                        message = "Payment is cancelled",
                        payment = null
                    )
                }

                Status.PARTIAL_CANCELLED -> {
                    val cancelledAmount = paymentDto.amount?.cancelled ?: 0
                    val cancelledAt = parseDateTime(paymentDto.cancelledAt)
                    // TODO: 이거 어떻게 돌아가는지 확인해봐야. 부분취소 개념을 정확히 이해 못한듯.
                    payment.cancelPartially(cancelledAmount, cancelledAt)
                    PaymentVerifyResult.failure("Payment is partially cancelled", paymentId)
                    VerifyPaymentResponse(
                        success = false,
                        message = "Payment is partially cancelled",
                        payment = null
                    )
                }
                else -> {
                    logger.warn("Not Expected status: status=${paymentDto.status}, paymentId=$paymentId")
                    VerifyPaymentResponse(
                        success = false,
                        message = "Payment is not complete",
                        payment = null
                    )
                }
            }
        } ?: run {
            logger.warn("Not Expected status: status=${paymentDto.status}, paymentId=$paymentId")
            VerifyPaymentResponse(
                success = false,
                message = "Payment is not complete",
                payment = null
            )
        }
    }



    suspend fun cancelPayment(
        paymentId: String,
        reason: String,
        amount: Int? = null  // null이면 전액 취소
    ): CancelPaymentResponse {
        logger.info("Payment Cancel Request: paymentId=$paymentId, reason=$reason, amount=$amount")

        val payment = paymentRepository.findByPaymentIdForUpdate(paymentId)
            ?: return CancelPaymentResponse(
                success = false,
                message = "Payment not found",
                payment = null
            )

        if (payment.status != Status.PAID && payment.status != Status.PARTIAL_CANCELLED) {
            return CancelPaymentResponse(
                success = false,
                message = "Payment status is not for cancel: ${payment.status}",
                payment = null
            )
        }

        val cancelAmount = amount ?: payment.getCancellableAmount()
        if (cancelAmount > payment.getCancellableAmount()) {
            return CancelPaymentResponse(
                success = false,
                message = "Cancel amount exceeds the cancellable amount. requested: $cancelAmount, cancellable: ${payment.getCancellableAmount()}",
                payment = null
            )
        }

        try {
            paymentGateway.cancelPayment(paymentId, reason, amount)
        } catch (e: PaymentFailException) {
            logger.error("PG 호출 실패: paymentId=$paymentId", e)
            return CancelPaymentResponse(
                success = false,
                message = "Payment Gateway cancel failed: ${e.message}",
                payment = null
            )
        }

        // DB 상태 업데이트
        if (amount == null || cancelAmount == payment.getCancellableAmount()) {
            payment.cancel(LocalDateTime.now())
        } else {
            payment.cancelPartially(cancelAmount, LocalDateTime.now())
        }

        logger.info("결제 취소 완료: paymentId=$paymentId, cancelledAmount=$cancelAmount")
        return CancelPaymentResponse(
            success = true,
            message = "Payment is cancelled",
            payment = PaymentDto.from(payment)
        )
    }

    fun getPayment(paymentId: String): PaymentEntity? {
        return paymentRepository.findByPaymentId(paymentId)
    }

    private fun parseDateTime(isoString: String?): LocalDateTime {
        if (isoString == null) return LocalDateTime.now()

        return try {
            LocalDateTime.parse(isoString)
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }

    data class PreparePaymentResponse(
        val paymentId: String,
        val storeId: String,
        val channelKey: String,
        val productName: String,
        val amount: Int,
        val currency: Currency
    )

    fun generatePaymentId(): String {
        val timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
        )
        val random = UUID.randomUUID().toString().take(4).uppercase()
        return "PAY-$timestamp-$random"
    }

    data class VerifyPaymentResponse(
        val success: Boolean,
        val message: String?,
        val payment: PaymentDto?
    )

    data class CancelPaymentResponse(
        val success: Boolean,
        val message: String?,
        val payment: PaymentDto?
    )

}