package beyondeyesight.domain.service.gathering

import beyondeyesight.config.isThirtyMinuteInterval
import beyondeyesight.config.uuidV7
import beyondeyesight.domain.exception.DataIntegrityException
import beyondeyesight.domain.exception.InvalidValueException
import beyondeyesight.domain.exception.LockAcquireFailException
import beyondeyesight.domain.exception.ResourceNotFoundException
import beyondeyesight.domain.exception.gathering.CannotJoinException
import beyondeyesight.domain.exception.gathering.CannotLeaveException
import beyondeyesight.domain.model.user.Gender
import beyondeyesight.domain.model.gathering.*
import beyondeyesight.domain.model.payment.PaymentEntity
import beyondeyesight.domain.model.payment.ProductType
import beyondeyesight.domain.model.payment.Status
import beyondeyesight.domain.repository.gathering.GuestRepository
import beyondeyesight.domain.repository.user.UserRepository
import beyondeyesight.domain.repository.gathering.GatheringRepository
import beyondeyesight.domain.repository.gathering.SeriesRepository
import beyondeyesight.domain.repository.gathering.SeriesScheduleRepository
import beyondeyesight.domain.repository.payment.PaymentRepository
import beyondeyesight.domain.service.LockService
import beyondeyesight.domain.service.payment.PaymentConfirmService
import beyondeyesight.domain.service.payment.PaymentGateway
import beyondeyesight.domain.service.payment.PaymentStateService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@Service
class GatheringService(
    private val gatheringRepository: GatheringRepository,
    private val guestService: GuestService,
    private val guestRepository: GuestRepository,
    private val lockService: LockService,
    private val userRepository: UserRepository,
    private val seriesRepository: SeriesRepository,
    private val seriesScheduleRepository: SeriesScheduleRepository,
    private val paymentConfirmService: PaymentConfirmService,
    private val paymentRepository: PaymentRepository,
    private val paymentStateService: PaymentStateService,
    private val paymentGateway: PaymentGateway,
) {
    val logger = LoggerFactory.getLogger(javaClass)
    fun open(
        hostUuid: UUID,
        approveType: GatheringEntity.ApproveType,
        minCapacity: Int,
        maxCapacity: Int,
        genderRatioEnabled: Boolean,
        minAge: Int,
        maxAge: Int,
        maxMaleCount: Int?,
        maxFemaleCount: Int?,
        fee: Int,
        discountEnabled: Boolean,
        offline: Boolean,
        place: String,
        category: Category,
        subCategory: SubCategory,
        imageUrl: String,
        title: String,
        introduction: String,
        startDateTime: LocalDateTime,
        duration: Duration?,
    ): GatheringEntity {
        validate(minAge, maxAge, maxMaleCount, maxFemaleCount, fee)

        val host = userRepository.findByUuid(hostUuid) ?: throw ResourceNotFoundException.byUuid(
            resourceName = "User",
            resourceUuid = hostUuid
        )

        val entity = GatheringEntity.open(
            hostUuid = host.uuid,
            approveType = approveType,
            minCapacity = minCapacity,
            maxCapacity = maxCapacity,
            genderRatioEnabled = genderRatioEnabled,
            minAge = minAge,
            maxAge = maxAge,
            maxMaleCount = maxMaleCount,
            maxFemaleCount = maxFemaleCount,
            fee = fee,
            discountEnabled = discountEnabled,
            offline = offline,
            place = place,
            category = category,
            subCategory = subCategory,
            imageUrl = imageUrl,
            title = title,
            introduction = introduction,
            startDateTime = startDateTime,
            score = 0,
            duration = duration
        )
        return gatheringRepository.save(entity)
    }


    private fun validate(
        minAge: Int,
        maxAge: Int,
        maxMaleCount: Int?,
        maxFemaleCount: Int?,
        fee: Int
    ) {
        if (minAge < 1) {
            throw InvalidValueException(
                valueName = "minAge",
                value = minAge,
                reason = null
            )
        }

        if (maxAge < minAge) {
            throw InvalidValueException(
                valueName = "maxAge",
                value = maxAge,
                reason = "maxAge must be greater than or equal to minAge"
            )
        }

        if (maxMaleCount != null && maxMaleCount < 0) {
            throw InvalidValueException(
                valueName = "maxMaleCount",
                value = maxMaleCount,
                reason = "maxMaleCount cannot be negative"
            )
        }

        if (maxFemaleCount != null && maxFemaleCount < 0) {
            throw InvalidValueException(
                valueName = "maxFemaleCount",
                value = maxFemaleCount,
                reason = "maxFemaleCount cannot be negative"
            )
        }

        if (fee % 1000 != 0) {
            throw InvalidValueException("fee", fee, "must be a multiple of 1000")
        }
    }

    fun close(uuid: UUID) {
        val gathering = gatheringRepository.findByUuid(uuid)
            ?: throw IllegalArgumentException("Gathering not found with uuid: $uuid")
        gathering.close()
        gatheringRepository.save(gathering)
    }

    // TODO: 부분 취소 적용
    fun leave(gatheringUuid: UUID, userUuid: UUID, reason: String) {
        guestService.leave(userUuid = userUuid, gatheringUuid = gatheringUuid)

        val paymentEntity =
            paymentRepository.findByProductTypeAndProductUuidAndBuyerUuid(
                productType = ProductType.GATHERING,
                productUuid = gatheringUuid,
                buyerUuid = userUuid,
            ) ?: return

        val pgPayment = paymentGateway.getPayment(paymentEntity.paymentId)

        try {
            if (pgPayment.status != Status.PAID && pgPayment.status != Status.PARTIAL_CANCELLED) {
                logger.warn("[3040] 사용자 $userUuid 님의 모임 $gatheringUuid 탈퇴 시도 시 결제 상태가 취소 가능한 상태가 아님: ${paymentEntity.status}")
                throw CannotLeaveException.invalidPaymentStatus(
                    paymentId = paymentEntity.paymentId,
                    status = pgPayment.status
                )
            }
            paymentStateService.cancelPayment(
                paymentId = paymentEntity.paymentId,
                reason = reason,
                amount = paymentEntity.getCancellableAmount()
            )

            logger.info("[3040] 사용자 $userUuid 님이 모임 $gatheringUuid 에서 나감.")
        } finally {
            paymentStateService.synchronize(paymentId = paymentEntity.paymentId)
        }

    }

    fun join(gatheringUuid: UUID, userUuid: UUID, paymentId: String, paymentToken: String, txId: String, amount: Int) {
        userRepository.findByUuid(userUuid) ?: throw ResourceNotFoundException.byUuid(
            resourceName = "User",
            resourceUuid = userUuid
        )
        val token = lockService.lockWithRetry(
            resourceName = "gathering",
            resourceId = gatheringUuid.toString(),
            expire = Duration.ofSeconds(10),
            waitTimeout = Duration.ofSeconds(20),
            retryInterval = Duration.ofMillis(100)
        ) ?: throw LockAcquireFailException.forResource(
            resourceName = GatheringEntity.RESOURCE_NAME,
            resourceUuid = gatheringUuid,
            duration = Duration.ofSeconds(20)
        )

        try {
            val gathering = gatheringRepository.findByUuid(gatheringUuid)
                ?: throw ResourceNotFoundException.byUuid(
                    resourceName = "Gathering",
                    resourceUuid = gatheringUuid
                )

            if (guestRepository.existsByUserUuidAndGatheringUuid(userUuid = userUuid, gatheringUuid = gathering.uuid)) {
                throw CannotJoinException.alreadyJoined(
                    userUuid = userUuid,
                    gatheringUuid = gathering.uuid,
                )
            }

            val currentGuestCount = guestRepository.countByGathering(gathering.uuid)

            if (currentGuestCount + 1 > gathering.maxCapacity) {
                throw CannotJoinException.full(
                    userUuid = userUuid,
                    gatheringUuid = gathering.uuid,
                )
            }

            if (gathering.genderRatioEnabled) {
                val currentMaleGuestCount = guestRepository.countByGatheringAndGender(gathering.uuid, Gender.M)
                val maxMaleCount = gathering.maxMaleCount ?: throw DataIntegrityException(
                    tableName = "gathering",
                    resourceUuid = gathering.uuid,
                    cause = "maxMaleCount must not be null when genderRatioEnabled is true"
                )

                if (currentMaleGuestCount + 1 > maxMaleCount) {
                    throw CannotJoinException.full(
                        userUuid = userUuid,
                        gatheringUuid = gathering.uuid,
                        gender = Gender.M
                    )
                }

                val currentFemaleGuestCount = guestRepository.countByGatheringAndGender(
                    gatheringUuid = gathering.uuid,
                    gender = Gender.F
                )
                val maxFemaleCount = gathering.maxFemaleCount ?: throw DataIntegrityException(
                    tableName = "gathering",
                    resourceUuid = gathering.uuid,
                    cause = "maxFemaleCount must not be null when genderRatioEnabled is true"
                )

                if (currentFemaleGuestCount + 1 > maxFemaleCount) {
                    throw CannotJoinException.full(
                        userUuid = userUuid,
                        gatheringUuid = gathering.uuid,
                        gender = Gender.F
                    )
                }
            }

            if (gathering.fee != amount) {
                throw CannotJoinException.priceChanged(
                    currentPrice = gathering.fee,
                    priceAtPay = amount
                )
            }

            guestService.join(
                gatheringUuid = gatheringUuid,
                userUuid = userUuid,
            )

            if (amount > 0) {
                paymentConfirmService.confirmPayment(
                    paymentId = paymentId,
                    paymentToken = paymentToken,
                    txId = txId,
                    amount = amount
                )
            }
        } finally {
            lockService.unlock("gathering", gatheringUuid.toString(), token)
        }
    }

    fun schedule(
        hostUuid: UUID,
        approveType: GatheringEntity.ApproveType,
        minCapacity: Int,
        maxCapacity: Int,
        genderRatioEnabled: Boolean,
        minAge: Int,
        maxAge: Int,
        fee: Int,
        discountEnabled: Boolean,
        offline: Boolean,
        place: String,
        category: Category,
        subCategory: SubCategory,
        imageUrl: String,
        title: String,
        introduction: String,
        scheduleType: ScheduleType,
        weeklySchedule: WeeklySchedule?,
        dateSchedule: DateSchedule?,
        gatheringDays: Int,
        maxMaleCount: Int?,
        maxFemaleCount: Int?
    ) {
        userRepository.findByUuid(hostUuid) ?: throw ResourceNotFoundException.byUuid(
            resourceName = "User",
            resourceUuid = hostUuid
        )

        val series = seriesRepository.save(
            SeriesEntity(
                uuid = uuidV7(),
                hostUuid = hostUuid,
                approveType = approveType,
                minCapacity = minCapacity,
                maxCapacity = maxCapacity,
                genderRatioEnabled = genderRatioEnabled,
                minAge = minAge,
                maxAge = maxAge,
                maxMaleCount = maxMaleCount,
                maxFemaleCount = maxFemaleCount,
                fee = fee,
                discountEnabled = discountEnabled,
                offline = offline,
                place = place,
                category = category,
                subCategory = subCategory,
                imageUrl = imageUrl,
                title = title,
                introduction = introduction,
            )
        )

        when (scheduleType) {
            ScheduleType.WEEKLY -> {
                if (weeklySchedule == null) {
                    throw InvalidValueException(
                        valueName = "weeklySchedule",
                        value = "null",
                        reason = "weeklySchedule must be provided for WEEKLY scheduleType"
                    )
                }
                if (weeklySchedule.summaries.isEmpty()) {
                    throw InvalidValueException(
                        valueName = "weeklySchedule.summaries",
                        value = "empty",
                        reason = "weeklySchedule.summaries must not be empty for WEEKLY scheduleType"
                    )
                }
                if (dateSchedule != null) {
                    throw InvalidValueException(
                        valueName = "dateSchedule",
                        value = "not null",
                        reason = "dateSchedule must be null for WEEKLY scheduleType"
                    )
                }

                for (summary in weeklySchedule.summaries) {
                    if (!summary.startTime.isThirtyMinuteInterval()) {
                        throw InvalidValueException(
                            valueName = "summary.startTime",
                            value = summary.startTime,
                            reason = "startTime must be in 30-minute intervals"
                        )
                    }
                    val openDayOfWeek = summary.startDayOfWeek.minus(gatheringDays.toLong())
                    seriesScheduleRepository.save(
                        SeriesScheduleEntity(
                            scheduleType = scheduleType,
                            openDayOfWeek = openDayOfWeek,
                            startDayOfWeek = summary.startDayOfWeek,
                            scheduleStartDate = weeklySchedule.startDate,
                            scheduleEndDate = weeklySchedule.endDate,
                            openDate = null,
                            startDate = null,
                            startTime = summary.startTime,
                            duration = summary.duration,
                            seriesEntity = series
                        )
                    )
                }
            }

            ScheduleType.DATE -> {
                if (dateSchedule == null) {
                    throw InvalidValueException(
                        valueName = "dateSchedule",
                        value = "null",
                        reason = "dateSchedule must be provided for DATE scheduleType"
                    )
                }
                if (dateSchedule.summaries.isEmpty()) {
                    throw InvalidValueException(
                        valueName = "dateSchedule.summaries",
                        value = "empty",
                        reason = "dateSchedule.summaries must not be empty for DATE scheduleType"
                    )
                }
                if (weeklySchedule != null) {
                    throw InvalidValueException(
                        valueName = "weeklySchedule",
                        value = "not null",
                        reason = "weeklySchedule must be null for DATE scheduleType"
                    )
                }

                for (summary in dateSchedule.summaries) {
                    if (!summary.startTime.isThirtyMinuteInterval()) {
                        throw InvalidValueException(
                            valueName = "summary.startTime",
                            value = summary.startTime,
                            reason = "startTime must be in 30-minute intervals"
                        )
                    }

                    val openDate = summary.startDate.minusDays(gatheringDays.toLong())
                    seriesScheduleRepository.save(
                        SeriesScheduleEntity(
                            scheduleType = scheduleType,
                            openDayOfWeek = null,
                            startDayOfWeek = null,
                            scheduleStartDate = null,
                            scheduleEndDate = null,
                            openDate = openDate,
                            startDate = summary.startDate,
                            startTime = summary.startTime,
                            duration = summary.duration,
                            seriesEntity = series
                        )
                    )
                }

            }
        }
    }
    fun handleFailedWebhook(paymentEntity: PaymentEntity) {
        val lockToken = lockService.lockWithRetry(
            resourceName = GatheringEntity.RESOURCE_NAME,
            resourceId = paymentEntity.productUuid.toString(),
            expire = Duration.ofSeconds(10),
            waitTimeout = Duration.ofSeconds(20),
            retryInterval = Duration.ofMillis(100)
        )?:run {
            logger.error("[3040] 결제 실패 웹훅 처리 실패: lock 획득 실패 paymentId=${paymentEntity.paymentId}, gatheringUuid=${paymentEntity.productUuid}")
            return
        }

        try {
            if (paymentEntity.status != Status.FAILED){
                logger.info("[3040] 실패 웹훅 처리: 이미 처리된 결제입니다. paymentId=${paymentEntity.paymentId}")
                return
            }

            val pgPayment = paymentGateway.getPayment(paymentEntity.paymentId)
            if (pgPayment.status != Status.FAILED) {
                logger.error("[3040] 실패 웹훅 처리 실패: PG 결제 상태가 FAILED가 아닙니다. paymentId=${paymentEntity.paymentId}, pgStatus=${pgPayment.status}")
                return
            }

            val gatheringUuid = paymentEntity.productUuid
            val userUuid = paymentEntity.buyerUuid

            if (!guestRepository.existsByUserUuidAndGatheringUuid(userUuid = userUuid, gatheringUuid = gatheringUuid)) {
                logger.info("[3040] 이미 모임에서 나간 사용자입니다. 결제 실패를 무시합니다. userUuid=$userUuid, gatheringUuid=$gatheringUuid")
                return
            }

            guestService.leave(
                gatheringUuid = gatheringUuid,
                userUuid = userUuid,
            )
            logger.info("[3040] 결제 실패 웹훅 처리: 모임 떠나기 처리 완료. userUuid=$userUuid, gatheringUuid=$gatheringUuid")
            paymentStateService.synchronize(paymentEntity.paymentId)
        } finally {
            lockService.unlock(
                resourceName = GatheringEntity.RESOURCE_NAME,
                resourceId = paymentEntity.paymentId,
                token = lockToken
            )
        }


    }

    fun handleCancelledWebhook(paymentEntity: PaymentEntity){
        val lockToken = lockService.lockWithRetry(
            resourceName = GatheringEntity.RESOURCE_NAME,
            resourceId = paymentEntity.productUuid.toString(),
            expire = Duration.ofSeconds(10),
            waitTimeout = Duration.ofSeconds(20),
            retryInterval = Duration.ofMillis(100)
        )?:run {
            logger.error("[3040] 결제 취소 웹훅 처리 실패: lock 획득 실패 paymentId=${paymentEntity.paymentId}, gatheringUuid=${paymentEntity.productUuid}")
            return
        }

        try {
            if (paymentEntity.status != Status.CANCELLED){
                logger.info("[3040] 취소 웹훅 처리: 이미 처리된 결제입니다. paymentId=${paymentEntity.paymentId}")
                return
            }

            val pgPayment = paymentGateway.getPayment(paymentEntity.paymentId)
            if (pgPayment.status != Status.CANCELLED) {
                logger.error("[3040] 취소 웹훅 처리 실패: PG 결제 상태가 CANCELLED가 아닙니다. paymentId=${paymentEntity.paymentId}, pgStatus=${pgPayment.status}")
                return
            }

            val gatheringUuid = paymentEntity.productUuid
            val userUuid = paymentEntity.buyerUuid

            if (!guestRepository.existsByUserUuidAndGatheringUuid(userUuid = userUuid, gatheringUuid = gatheringUuid)) {
                logger.info("[3040] 이미 모임에서 나간 사용자입니다. 취소를 무시합니다. userUuid=$userUuid, gatheringUuid=$gatheringUuid")
                return
            }

            guestService.leave(
                gatheringUuid = gatheringUuid,
                userUuid = userUuid,
            )
            logger.info("[3040] 결제 취소 웹훅 처리: 모임 떠나기 처리 완료. userUuid=$userUuid, gatheringUuid=$gatheringUuid")
            paymentStateService.synchronize(paymentEntity.paymentId)
        } finally {

            lockService.unlock(
                resourceName = GatheringEntity.RESOURCE_NAME,
                resourceId = paymentEntity.paymentId,
                token = lockToken
            )
        }
    }

    fun handlePartialCancelledWebhook(paymentEntity: PaymentEntity) {
        val lockToken = lockService.lockWithRetry(
            resourceName = GatheringEntity.RESOURCE_NAME,
            resourceId = paymentEntity.productUuid.toString(),
            expire = Duration.ofSeconds(10),
            waitTimeout = Duration.ofSeconds(20),
            retryInterval = Duration.ofMillis(100)
        )?:run {
            logger.error("[3040] 결제 부분취소 웹훅 처리 실패: lock 획득 실패 paymentId=${paymentEntity.paymentId}, gatheringUuid=${paymentEntity.productUuid}")
            return
        }
        try {
            if (paymentEntity.status != Status.PARTIAL_CANCELLED){
                logger.info("[3040] 부분 취소 웹훅 처리: 이미 처리된 결제입니다. paymentId=${paymentEntity.paymentId}")
                return
            }

            val pgPayment = paymentGateway.getPayment(paymentEntity.paymentId)
            if (pgPayment.status != Status.PARTIAL_CANCELLED) {
                logger.error("[3040] 부분 취소 웹훅 처리 실패: PG 결제 상태가 PARTIAL_CANCELLED가 아닙니다. paymentId=${paymentEntity.paymentId}, pgStatus=${pgPayment.status}")
                return
            }

            val gatheringUuid = paymentEntity.productUuid
            val userUuid = paymentEntity.buyerUuid

            if (!guestRepository.existsByUserUuidAndGatheringUuid(userUuid = userUuid, gatheringUuid = gatheringUuid)) {
                logger.info("[3040] 이미 모임에서 나간 사용자입니다. 부분 취소를 무시합니다. userUuid=$userUuid, gatheringUuid=$gatheringUuid")
                return
            }

            guestService.leave(
                gatheringUuid = gatheringUuid,
                userUuid = userUuid,
            )
            logger.info("[3040] 결제 부분취소 웹훅 처리: 모임 떠나기 처리 완료. userUuid=$userUuid, gatheringUuid=$gatheringUuid")
            paymentStateService.synchronize(paymentEntity.paymentId)
        } finally {

            lockService.unlock(
                resourceName = GatheringEntity.RESOURCE_NAME,
                resourceId = paymentEntity.paymentId,
                token = lockToken
            )
        }
    }

    fun handlePaidWebhook(paymentEntity: PaymentEntity) {
        val lockToken = lockService.lockWithRetry(
            resourceName = GatheringEntity.RESOURCE_NAME,
            resourceId = paymentEntity.productUuid.toString(),
            expire = Duration.ofSeconds(10),
            waitTimeout = Duration.ofSeconds(20),
            retryInterval = Duration.ofMillis(100)
        )?:run {
            logger.error("[3040] 결제 완료 웹훅 처리 실패: lock 획득 실패 paymentId=${paymentEntity.paymentId}, gatheringUuid=${paymentEntity.productUuid}")
            return
        }

        try {
            if (paymentEntity.status == Status.PAID){
                logger.info("[3040] 결제 완료 웹훅 처리: 이미 처리된 결제입니다. paymentId=${paymentEntity.paymentId}")
                return
            }

            val pgPayment = paymentGateway.getPayment(paymentEntity.paymentId)
            if (pgPayment.status != Status.PAID) {
                logger.error("[3040] 결제 완료 웹훅 처리 실패: PG 결제 상태가 PAID가 아닙니다. paymentId=${paymentEntity.paymentId}, pgStatus=${pgPayment.status}")
                return
            }

            val gatheringUuid = paymentEntity.productUuid
            val userUuid = paymentEntity.buyerUuid

            val gathering = gatheringRepository.findByUuid(gatheringUuid)
                ?: run {
                    logger.error("[3040] 결제 완료 웹훅 처리 실패: 모임을 찾을 수 없습니다. 취소 처리합니다. gatheringUuid=$gatheringUuid, paymentId=${paymentEntity.paymentId}")
                    paymentGateway.cancelPayment(
                        paymentId = paymentEntity.paymentId,
                        reason = "모임을 찾을 수 없습니다. gatheringUuid=${gatheringUuid}, userUuid=${userUuid}",
                        amount = null
                    )
                    return
                }

            if (guestRepository.existsByUserUuidAndGatheringUuid(userUuid = userUuid, gatheringUuid = gatheringUuid)) {
                logger.info("[3040] 결제 완료 웹훅 처리: 이미 모임에 참가한 사용자입니다. userUuid=$userUuid, gatheringUuid=$gatheringUuid")
                return
            }

            val currentGuestCount = guestRepository.countByGathering(gathering.uuid)

            if (currentGuestCount + 1 > gathering.maxCapacity) {
                logger.error("[3040] 결제 완료 웹훅 처리 실패: 모임 정원이 초과되었습니다. 취소 처리합니다. gatheringUuid=$gatheringUuid, paymentId=${paymentEntity.paymentId}")
                paymentGateway.cancelPayment(
                    paymentId = paymentEntity.paymentId,
                    reason = "정원 초과. gatheringUuid=${gatheringUuid}, userUuid=${userUuid}",
                    amount = null
                )
                return
            }

            if (gathering.genderRatioEnabled && gathering.maxMaleCount != null && gathering.maxFemaleCount != null) {
                val currentMaleGuestCount = guestRepository.countByGatheringAndGender(gathering.uuid, Gender.M)
                val maxMaleCount = gathering.maxMaleCount ?: throw DataIntegrityException(
                    tableName = "gathering",
                    resourceUuid = gathering.uuid,
                    cause = "maxMaleCount must not be null when genderRatioEnabled is true"
                )

                if (currentMaleGuestCount + 1 > maxMaleCount) {
                    logger.error("[3040] 결제 완료 웹훅 처리 실패: 모임 정원이 초과되었습니다. 취소 처리합니다. gatheringUuid=$gatheringUuid, paymentId=${paymentEntity.paymentId}")
                    paymentGateway.cancelPayment(
                        paymentId = paymentEntity.paymentId,
                        reason = "정원 초과 M. gatheringUuid=${gatheringUuid}, userUuid=${userUuid}",
                        amount = null
                    )
                }

                val currentFemaleGuestCount = guestRepository.countByGatheringAndGender(
                    gatheringUuid = gathering.uuid,
                    gender = Gender.F
                )
                val maxFemaleCount = gathering.maxFemaleCount ?: throw DataIntegrityException(
                    tableName = "gathering",
                    resourceUuid = gathering.uuid,
                    cause = "maxFemaleCount must not be null when genderRatioEnabled is true"
                )

                if (currentFemaleGuestCount + 1 > maxFemaleCount) {
                    logger.error("[3040] 결제 완료 웹훅 처리 실패: 모임 정원이 초과되었습니다. 취소 처리합니다. gatheringUuid=$gatheringUuid, paymentId=${paymentEntity.paymentId}")
                    paymentGateway.cancelPayment(
                        paymentId = paymentEntity.paymentId,
                        reason = "정원 초과 F. gatheringUuid=${gatheringUuid}, userUuid=${userUuid}",
                        amount = null
                    )
                }
            }

            guestService.join(
                gatheringUuid = gatheringUuid,
                userUuid = userUuid,
            )
            logger.info("[3040] 결제 완료 웹훅 처리: 모임 참가 처리 완료. userUuid=$userUuid, gatheringUuid=$gatheringUuid")
            paymentStateService.synchronize(paymentEntity.paymentId)
        } finally {
            lockService.unlock(
                resourceName = GatheringEntity.RESOURCE_NAME,
                resourceId = paymentEntity.paymentId,
                token = lockToken
            )
        }
    }
}