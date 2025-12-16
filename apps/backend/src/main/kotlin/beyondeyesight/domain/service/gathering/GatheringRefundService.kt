package beyondeyesight.domain.service.gathering

import beyondeyesight.domain.exception.ResourceNotFoundException
import beyondeyesight.domain.exception.payment.CannotCancelException
import beyondeyesight.domain.model.GuestEntity
import beyondeyesight.domain.model.GuestId
import beyondeyesight.domain.model.payment.PaymentEntity
import beyondeyesight.domain.model.payment.ProductType
import beyondeyesight.domain.model.payment.Status
import beyondeyesight.domain.repository.gathering.GatheringRepository
import beyondeyesight.domain.repository.gathering.GuestRepository
import beyondeyesight.domain.repository.payment.PaymentRepository
import beyondeyesight.domain.service.payment.PaymentGateway
import beyondeyesight.domain.service.payment.PaymentStateService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class GatheringRefundService(
    private val guestRepository: GuestRepository,
    private val gatheringRepository: GatheringRepository,
    private val paymentGateway: PaymentGateway,
    private val paymentStateService: PaymentStateService,
) {
    val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun refund(userUuid: UUID, gatheringUuid: UUID, reason: String, paymentEntity: PaymentEntity, amount: Int) {

        val pgPayment = paymentGateway.getPayment(paymentEntity.paymentId)
        if (pgPayment.status != Status.PAID) {

            logger.error("[3040] 사용자 $userUuid 님의 모임 $gatheringUuid 나가기 시도 시 결제 상태가 취소 가능한 상태가 아님: ${paymentEntity.status}")
            throw CannotCancelException.invalidPaymentStatus(
                paymentId = paymentEntity.paymentId,
                status = pgPayment.status
            )
        }

        val refundAmount = calculateRefundAmount(
            gatheringUuid = gatheringUuid,
            userUuid = userUuid,
            amount = amount
        )

        paymentGateway.cancelPayment(
            paymentId = paymentEntity.paymentId,
            reason = reason,
            amount = refundAmount
        )
        logger.info("[3040] 사용자 $userUuid 님의 모임 $gatheringUuid 를 나갈 때 환불받음: ${refundAmount}")
        paymentStateService.synchronize(paymentId = paymentEntity.paymentId)
    }

    private fun calculateRefundAmount(gatheringUuid: UUID, userUuid: UUID, amount: Int): Int {
        val now = LocalDateTime.now()
        val guestId = GuestId(gatheringUuid = gatheringUuid, userUuid = userUuid)
        val guest = guestRepository.findByGuestId(guestId)
            ?: throw ResourceNotFoundException.byField(
                resourceName = GuestEntity.RESOURCE_NAME,
                fieldName = "guestId",
                fieldValue = guestId
            )

        if (guest.joinedAt.plusMinutes(30) < now) {
            return amount
        }

        val gathering = gatheringRepository.findByUuid(gatheringUuid) ?: throw ResourceNotFoundException.byUuid(
            resourceName = "Gathering",
            resourceUuid = gatheringUuid
        )

        if (gathering.startDateTime.minusDays(2).isBefore(now)) {
            return 0
        }

        if (gathering.startDateTime.minusDays(4).isBefore(now)) {
            return amount * 90 / 100
        }

        return amount
    }
}