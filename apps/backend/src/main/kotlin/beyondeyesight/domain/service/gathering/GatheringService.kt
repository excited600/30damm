package beyondeyesight.domain.service.gathering

import beyondeyesight.config.isThirtyMinuteInterval
import beyondeyesight.config.uuidV7
import beyondeyesight.domain.exception.DataIntegrityException
import beyondeyesight.domain.exception.InvalidValueException
import beyondeyesight.domain.exception.LockAcquireFailException
import beyondeyesight.domain.exception.ResourceNotFoundException
import beyondeyesight.domain.exception.gathering.CannotJoinException
import beyondeyesight.domain.model.GuestEntity
import beyondeyesight.domain.model.GuestId
import beyondeyesight.domain.model.gathering.*
import beyondeyesight.domain.model.payment.ConfirmPaymentRequest
import beyondeyesight.domain.model.payment.PaymentEntity
import beyondeyesight.domain.model.payment.ProductType
import beyondeyesight.domain.model.payment.Status
import beyondeyesight.domain.model.user.Gender
import beyondeyesight.domain.repository.gathering.GatheringRepository
import beyondeyesight.domain.repository.gathering.GuestRepository
import beyondeyesight.domain.repository.gathering.SeriesRepository
import beyondeyesight.domain.repository.gathering.SeriesScheduleRepository
import beyondeyesight.domain.repository.payment.PaymentRepository
import beyondeyesight.domain.repository.user.UserRepository
import beyondeyesight.domain.service.LockService
import beyondeyesight.domain.service.payment.PaymentService
import beyondeyesight.domain.service.payment.PaymentGateway
import beyondeyesight.domain.service.payment.PaymentSynchronizeService
import beyondeyesight.domain.model.user.UserEntity
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
    private val paymentService: PaymentService,
    private val paymentRepository: PaymentRepository,
    private val paymentSynchronizeService: PaymentSynchronizeService,
    private val paymentGateway: PaymentGateway,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    data class GuestWithUser(val guest: beyondeyesight.domain.model.GuestEntity, val user: UserEntity)
    data class GenderCounts(val maleCount: Int, val femaleCount: Int)
    data class GatheringDetail(
        val gathering: GatheringEntity,
        val host: UserEntity,
        val guestsWithUsers: List<GuestWithUser>,
        val genderCounts: GenderCounts,
        val userStatus: GatheringUserStatus,
    )
    data class ScrollWithDetails(
        val scrollResult: beyondeyesight.domain.model.ScrollResult<GatheringEntity, GatheringCursor>,
        val hostUsers: Map<UUID, UserEntity>,
        val genderCountsMap: Map<UUID, GenderCounts>,
    )

    fun open(
        hostUuid: UUID,
        title: String,
        description: String?,
        category: Category,
        location: String?,
        startDateTime: LocalDateTime?,
        duration: Duration?,
        minCapacity: Int,
        maxCapacity: Int,
        genderRatioEnabled: Boolean,
        maxMaleCount: Int?,
        maxFemaleCount: Int?,
        isFree: Boolean,
        price: Int?,
        isSplit: Boolean,
        imageUrl: String?,
    ): GatheringEntity {
        val host = userRepository.findByUuid(hostUuid) ?: throw ResourceNotFoundException.byUuid(
            resourceName = "User",
            resourceUuid = hostUuid
        )

        val entity = GatheringEntity.open(
            hostUuid = host.uuid,
            title = title,
            description = description,
            category = category,
            location = location,
            startDateTime = startDateTime,
            duration = duration,
            minCapacity = minCapacity,
            maxCapacity = maxCapacity,
            genderRatioEnabled = genderRatioEnabled,
            maxMaleCount = maxMaleCount,
            maxFemaleCount = maxFemaleCount,
            isFree = isFree,
            price = price,
            isSplit = isSplit,
            imageUrl = imageUrl,
        )
        return gatheringRepository.save(entity)
    }

    fun close(uuid: UUID) {
        val gathering = gatheringRepository.findByUuid(uuid)
            ?: throw IllegalArgumentException("Gathering not found with uuid: $uuid")
        gathering.close()
        gatheringRepository.save(gathering)
    }

    fun getDetail(userUuid: UUID, gatheringUuid: UUID): GatheringDetail {
        val gathering = gatheringRepository.findByUuid(gatheringUuid)
            ?: throw ResourceNotFoundException.byUuid(
                resourceName = GatheringEntity.RESOURCE_NAME,
                resourceUuid = gatheringUuid
            )

        val host = userRepository.findByUuid(gathering.hostUuid)
            ?: throw ResourceNotFoundException.byUuid(
                resourceName = UserEntity.RESOURCE_NAME,
                resourceUuid = gathering.hostUuid
            )

        val guests = guestRepository.findAllByGatheringUuid(gatheringUuid)
        val guestsWithUsers = guests.mapNotNull { guest ->
            val user = userRepository.findByUuid(guest.userUuid) ?: return@mapNotNull null
            GuestWithUser(guest, user)
        }

        val genderCounts = countGendersByGathering(gatheringUuid)

        val userStatus = when {
            userUuid == host.uuid -> GatheringUserStatus.HOST_OPENED
            guestsWithUsers.any { it.user.uuid == userUuid } -> GatheringUserStatus.GUEST_JOINED
            else -> GatheringUserStatus.GUEST_NOT_JOINED
        }

        return GatheringDetail(
            gathering = gathering,
            host = host,
            guestsWithUsers = guestsWithUsers,
            genderCounts = genderCounts,
            userStatus = userStatus
        )
    }

    fun scroll(
        cursor: GatheringCursor?,
        size: Int,
        filter: GatheringFilter,
    ): ScrollWithDetails {
        val result = gatheringRepository.scroll(
            cursor = cursor,
            size = size,
            filter = filter,
        )

        val hostUuids = result.items.map { it.hostUuid }.distinct()
        val hostUsers = hostUuids.mapNotNull { userRepository.findByUuid(it) }.associateBy { it.uuid }

        val genderCountsMap = result.items.associate { gathering ->
            gathering.uuid to countGendersByGathering(gathering.uuid)
        }

        return ScrollWithDetails(
            scrollResult = result,
            hostUsers = hostUsers,
            genderCountsMap = genderCountsMap,
        )
    }

    private fun countGendersByGathering(gatheringUuid: UUID): GenderCounts {
        return GenderCounts(
            maleCount = guestRepository.countByGatheringAndGender(gatheringUuid, Gender.MALE).toInt(),
            femaleCount = guestRepository.countByGatheringAndGender(gatheringUuid, Gender.FEMALE).toInt()
        )
    }

    fun leave(gatheringUuid: UUID, userUuid: UUID, reason: String) {
        guestService.leave(userUuid = userUuid, gatheringUuid = gatheringUuid)
        logger.info("[30damm] 사용자 $userUuid 님이 모임 $gatheringUuid 에서 나감.")

        val paymentEntity = paymentRepository.findByProductTypeAndProductUuidAndBuyerUuid(
            productType = ProductType.GATHERING,
            productUuid = gatheringUuid,
            buyerUuid = userUuid,
        ) ?: return

        if (paymentEntity.amount > 0) {
            refund(
                userUuid = userUuid,
                gatheringUuid = gatheringUuid,
                reason = reason,
                paymentId = paymentEntity.paymentId,
                amount = paymentEntity.amount
            )
        }
    }

    fun join(gatheringUuid: UUID, userUuid: UUID, confirmPaymentRequest: ConfirmPaymentRequest?) {
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

            if (guestRepository.existsByGuestId(GuestId(gatheringUuid = gatheringUuid, userUuid = userUuid))) {
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
                val currentMaleGuestCount = guestRepository.countByGatheringAndGender(gathering.uuid, Gender.MALE)
                val maxMaleCount = gathering.maxMaleCount ?: throw DataIntegrityException(
                    tableName = "gathering",
                    resourceUuid = gathering.uuid,
                    cause = "maxMaleCount must not be null when genderRatioEnabled is true"
                )

                if (currentMaleGuestCount + 1 > maxMaleCount) {
                    throw CannotJoinException.full(
                        userUuid = userUuid,
                        gatheringUuid = gathering.uuid,
                        gender = Gender.MALE
                    )
                }

                val currentFemaleGuestCount = guestRepository.countByGatheringAndGender(
                    gatheringUuid = gathering.uuid,
                    gender = Gender.FEMALE
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
                        gender = Gender.FEMALE
                    )
                }
            }

            if (gathering.isFree() && confirmPaymentRequest != null) {
                throw InvalidValueException(
                    valueName = "confirmPaymentRequest",
                    value = confirmPaymentRequest,
                    reason = "confirmPaymentRequest must be null when gathering is free"
                )
            }

            if (!gathering.isFree() && confirmPaymentRequest == null) {
                throw InvalidValueException(
                    valueName = "confirmPaymentRequest",
                    value = "null",
                    reason = "confirmPaymentRequest must not be null when gathering is not free"
                )
            }

            guestService.join(
                gatheringUuid = gatheringUuid,
                userUuid = userUuid,
            )

            if (!gathering.isFree() && confirmPaymentRequest != null) {
                if (gathering.fee != confirmPaymentRequest.amount) {
                    throw CannotJoinException.priceChanged(
                        currentPrice = gathering.fee,
                        priceAtPay = confirmPaymentRequest.amount
                    )
                }
                if (confirmPaymentRequest.amount > 0) {
                    paymentService.confirmPayment(
                        paymentId = confirmPaymentRequest.paymentId,
                        paymentToken = confirmPaymentRequest.paymentToken,
                        txId = confirmPaymentRequest.txId,
                        amount = confirmPaymentRequest.amount
                    )
                }
            }
        } finally {
            lockService.unlock("gathering", gatheringUuid.toString(), token)
        }
    }

    fun schedule(
        hostUuid: UUID,
        minCapacity: Int,
        maxCapacity: Int,
        genderRatioEnabled: Boolean,
        fee: Int,
        isSplit: Boolean,
        place: String?,
        category: Category,
        imageUrl: String?,
        title: String,
        description: String?,
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
                minCapacity = minCapacity,
                maxCapacity = maxCapacity,
                genderRatioEnabled = genderRatioEnabled,
                maxMaleCount = maxMaleCount,
                maxFemaleCount = maxFemaleCount,
                fee = fee,
                isSplit = isSplit,
                place = place,
                category = category,
                imageUrl = imageUrl,
                title = title,
                description = description,
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
        ) ?: run {
            logger.error("[30damm] 결제 실패 웹훅 처리 실패: lock 획득 실패 paymentId=${paymentEntity.paymentId}, gatheringUuid=${paymentEntity.productUuid}")
            return
        }

        try {
            if (paymentEntity.status != Status.FAILED) {
                logger.info("[30damm] 실패 웹훅 처리: 이미 처리된 결제입니다. paymentId=${paymentEntity.paymentId}")
                return
            }

            val pgPayment = paymentGateway.getPayment(paymentEntity.paymentId)
            if (pgPayment.status != Status.FAILED) {
                logger.error("[30damm] 실패 웹훅 처리 실패: PG 결제 상태가 FAILED가 아닙니다. paymentId=${paymentEntity.paymentId}, pgStatus=${pgPayment.status}")
                return
            }

            val gatheringUuid = paymentEntity.productUuid
            val userUuid = paymentEntity.buyerUuid

            if (!guestRepository.existsByGuestId(GuestId(gatheringUuid = gatheringUuid, userUuid = userUuid))) {
                logger.info("[30damm] 이미 모임에서 나간 사용자입니다. 결제 실패를 무시합니다. userUuid=$userUuid, gatheringUuid=$gatheringUuid")
                return
            }

            guestService.leave(
                gatheringUuid = gatheringUuid,
                userUuid = userUuid,
            )
            logger.info("[30damm] 결제 실패 웹훅 처리: 모임 떠나기 처리 완료. userUuid=$userUuid, gatheringUuid=$gatheringUuid")
            paymentSynchronizeService.synchronize(paymentEntity.paymentId)
        } finally {
            lockService.unlock(
                resourceName = GatheringEntity.RESOURCE_NAME,
                resourceId = paymentEntity.productUuid.toString(),
                token = lockToken
            )
        }
    }

    fun handleCancelledWebhook(paymentEntity: PaymentEntity) {
        val lockToken = lockService.lockWithRetry(
            resourceName = GatheringEntity.RESOURCE_NAME,
            resourceId = paymentEntity.productUuid.toString(),
            expire = Duration.ofSeconds(10),
            waitTimeout = Duration.ofSeconds(20),
            retryInterval = Duration.ofMillis(100)
        ) ?: run {
            logger.error("[30damm] 결제 취소 웹훅 처리 실패: lock 획득 실패 paymentId=${paymentEntity.paymentId}, gatheringUuid=${paymentEntity.productUuid}")
            return
        }

        try {
            if (paymentEntity.status != Status.CANCELLED) {
                logger.info("[30damm] 취소 웹훅 처리: 이미 처리된 결제입니다. paymentId=${paymentEntity.paymentId}")
                return
            }

            val pgPayment = paymentGateway.getPayment(paymentEntity.paymentId)
            if (pgPayment.status != Status.CANCELLED) {
                logger.error("[30damm] 취소 웹훅 처리 실패: PG 결제 상태가 CANCELLED가 아닙니다. paymentId=${paymentEntity.paymentId}, pgStatus=${pgPayment.status}")
                return
            }

            val gatheringUuid = paymentEntity.productUuid
            val userUuid = paymentEntity.buyerUuid

            if (!guestRepository.existsByGuestId(GuestId(gatheringUuid = gatheringUuid, userUuid = userUuid))) {
                logger.info("[30damm] 이미 모임에서 나간 사용자입니다. 취소를 무시합니다. userUuid=$userUuid, gatheringUuid=$gatheringUuid")
                return
            }

            guestService.leave(
                gatheringUuid = gatheringUuid,
                userUuid = userUuid,
            )
            logger.info("[30damm] 결제 취소 웹훅 처리: 모임 떠나기 처리 완료. userUuid=$userUuid, gatheringUuid=$gatheringUuid")
            paymentSynchronizeService.synchronize(paymentEntity.paymentId)
        } finally {
            lockService.unlock(
                resourceName = GatheringEntity.RESOURCE_NAME,
                resourceId = paymentEntity.productUuid.toString(),
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
        ) ?: run {
            logger.error("[30damm] 결제 부분취소 웹훅 처리 실패: lock 획득 실패 paymentId=${paymentEntity.paymentId}, gatheringUuid=${paymentEntity.productUuid}")
            return
        }
        try {
            if (paymentEntity.status != Status.PARTIAL_CANCELLED) {
                logger.info("[30damm] 부분 취소 웹훅 처리: 이미 처리된 결제입니다. paymentId=${paymentEntity.paymentId}")
                return
            }

            val pgPayment = paymentGateway.getPayment(paymentEntity.paymentId)
            if (pgPayment.status != Status.PARTIAL_CANCELLED) {
                logger.error("[30damm] 부분 취소 웹훅 처리 실패: PG 결제 상태가 PARTIAL_CANCELLED가 아닙니다. paymentId=${paymentEntity.paymentId}, pgStatus=${pgPayment.status}")
                return
            }

            val gatheringUuid = paymentEntity.productUuid
            val userUuid = paymentEntity.buyerUuid

            if (!guestRepository.existsByGuestId(GuestId(gatheringUuid = gatheringUuid, userUuid = userUuid))) {
                logger.info("[30damm] 이미 모임에서 나간 사용자입니다. 부분 취소를 무시합니다. userUuid=$userUuid, gatheringUuid=$gatheringUuid")
                return
            }

            guestService.leave(
                gatheringUuid = gatheringUuid,
                userUuid = userUuid,
            )
            logger.info("[30damm] 결제 부분취소 웹훅 처리: 모임 떠나기 처리 완료. userUuid=$userUuid, gatheringUuid=$gatheringUuid")
            paymentSynchronizeService.synchronize(paymentEntity.paymentId)
        } finally {
            lockService.unlock(
                resourceName = GatheringEntity.RESOURCE_NAME,
                resourceId = paymentEntity.productUuid.toString(),
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
        ) ?: run {
            logger.error("[30damm] 결제 완료 웹훅 처리 실패: lock 획득 실패 paymentId=${paymentEntity.paymentId}, gatheringUuid=${paymentEntity.productUuid}")
            return
        }

        try {
            if (paymentEntity.status == Status.PAID) {
                logger.info("[30damm] 결제 완료 웹훅 처리: 이미 처리된 결제입니다. paymentId=${paymentEntity.paymentId}")
                return
            }

            val pgPayment = paymentGateway.getPayment(paymentEntity.paymentId)
            if (pgPayment.status != Status.PAID) {
                logger.error("[30damm] 결제 완료 웹훅 처리 실패: PG 결제 상태가 PAID가 아닙니다. paymentId=${paymentEntity.paymentId}, pgStatus=${pgPayment.status}")
                return
            }

            val gatheringUuid = paymentEntity.productUuid
            val userUuid = paymentEntity.buyerUuid

            val gathering = gatheringRepository.findByUuid(gatheringUuid)
                ?: run {
                    logger.error("[30damm] 결제 완료 웹훅 처리 실패: 모임을 찾을 수 없습니다. 취소 처리합니다. gatheringUuid=$gatheringUuid, paymentId=${paymentEntity.paymentId}")
                    paymentGateway.cancelPayment(
                        paymentId = paymentEntity.paymentId,
                        reason = "모임을 찾을 수 없습니다. gatheringUuid=${gatheringUuid}, userUuid=${userUuid}",
                        amount = null
                    )
                    return
                }

            if (guestRepository.existsByGuestId(GuestId(gatheringUuid = gatheringUuid, userUuid = userUuid))) {
                logger.info("[30damm] 결제 완료 웹훅 처리: 이미 모임에 참가한 사용자입니다. userUuid=$userUuid, gatheringUuid=$gatheringUuid")
                return
            }

            val currentGuestCount = guestRepository.countByGathering(gathering.uuid)

            if (currentGuestCount + 1 > gathering.maxCapacity) {
                logger.error("[30damm] 결제 완료 웹훅 처리 실패: 모임 정원이 초과되었습니다. 취소 처리합니다. gatheringUuid=$gatheringUuid, paymentId=${paymentEntity.paymentId}")
                paymentGateway.cancelPayment(
                    paymentId = paymentEntity.paymentId,
                    reason = "정원 초과. gatheringUuid=${gatheringUuid}, userUuid=${userUuid}",
                    amount = null
                )
                return
            }

            if (gathering.genderRatioEnabled && gathering.maxMaleCount != null && gathering.maxFemaleCount != null) {
                val currentMaleGuestCount = guestRepository.countByGatheringAndGender(gathering.uuid, Gender.MALE)
                val maxMaleCount = gathering.maxMaleCount ?: throw DataIntegrityException(
                    tableName = "gathering",
                    resourceUuid = gathering.uuid,
                    cause = "maxMaleCount must not be null when genderRatioEnabled is true"
                )

                if (currentMaleGuestCount + 1 > maxMaleCount) {
                    logger.error("[30damm] 결제 완료 웹훅 처리 실패: 모임 정원이 초과되었습니다. 취소 처리합니다. gatheringUuid=$gatheringUuid, paymentId=${paymentEntity.paymentId}")
                    paymentGateway.cancelPayment(
                        paymentId = paymentEntity.paymentId,
                        reason = "정원 초과 M. gatheringUuid=${gatheringUuid}, userUuid=${userUuid}",
                        amount = null
                    )
                    return
                }

                val currentFemaleGuestCount = guestRepository.countByGatheringAndGender(
                    gatheringUuid = gathering.uuid,
                    gender = Gender.FEMALE
                )
                val maxFemaleCount = gathering.maxFemaleCount ?: throw DataIntegrityException(
                    tableName = "gathering",
                    resourceUuid = gathering.uuid,
                    cause = "maxFemaleCount must not be null when genderRatioEnabled is true"
                )

                if (currentFemaleGuestCount + 1 > maxFemaleCount) {
                    logger.error("[30damm] 결제 완료 웹훅 처리 실패: 모임 정원이 초과되었습니다. 취소 처리합니다. gatheringUuid=$gatheringUuid, paymentId=${paymentEntity.paymentId}")
                    paymentGateway.cancelPayment(
                        paymentId = paymentEntity.paymentId,
                        reason = "정원 초과 F. gatheringUuid=${gatheringUuid}, userUuid=${userUuid}",
                        amount = null
                    )
                    return
                }
            }

            guestService.join(
                gatheringUuid = gatheringUuid,
                userUuid = userUuid,
            )
            logger.info("[30damm] 결제 완료 웹훅 처리: 모임 참가 처리 완료. userUuid=$userUuid, gatheringUuid=$gatheringUuid")
            paymentSynchronizeService.synchronize(paymentEntity.paymentId)
        } finally {
            lockService.unlock(
                resourceName = GatheringEntity.RESOURCE_NAME,
                resourceId = paymentEntity.productUuid.toString(),
                token = lockToken
            )
        }
    }

    private fun refund(userUuid: UUID, gatheringUuid: UUID, reason: String, paymentId: String, amount: Int) {
        val refundAmount = calculateRefundAmount(
            gatheringUuid = gatheringUuid,
            userUuid = userUuid,
            amount = amount
        )

        paymentService.cancelPayment(
            paymentId = paymentId,
            reason = reason,
            amount = refundAmount
        )
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

        if (now <= guest.joinedAt.plusMinutes(30)) {
            return amount
        }

        val gathering = gatheringRepository.findByUuid(gatheringUuid) ?: throw ResourceNotFoundException.byUuid(
            resourceName = "Gathering",
            resourceUuid = gatheringUuid
        )

        val startDateTime = gathering.startDateTime ?: return amount

        if (startDateTime.minusDays(2).isBefore(now)) {
            return 0
        }

        if (startDateTime.minusDays(4).isBefore(now)) {
            return amount * 90 / 100
        }

        return amount
    }
}
