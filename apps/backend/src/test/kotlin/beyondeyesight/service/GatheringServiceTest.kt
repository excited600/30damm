package beyondeyesight.service

import beyondeyesight.domain.exception.DataIntegrityException
import beyondeyesight.domain.exception.InvalidValueException
import beyondeyesight.domain.exception.LockAcquireFailException
import beyondeyesight.domain.exception.ResourceNotFoundException
import beyondeyesight.domain.exception.gathering.CannotJoinException
import beyondeyesight.domain.model.gathering.*
import beyondeyesight.domain.model.user.Gender
import beyondeyesight.domain.model.user.UserEntity
import beyondeyesight.domain.repository.gathering.GatheringRepository
import beyondeyesight.domain.repository.gathering.GuestRepository
import beyondeyesight.domain.repository.gathering.SeriesRepository
import beyondeyesight.domain.repository.gathering.SeriesScheduleRepository
import beyondeyesight.domain.repository.user.UserRepository
import beyondeyesight.domain.service.LockService
import beyondeyesight.domain.service.gathering.GatheringService
import beyondeyesight.domain.service.gathering.GuestService
import beyondeyesight.domain.service.payment.PaymentGateway
import beyondeyesight.domain.service.payment.PaymentService
import beyondeyesight.domain.service.payment.PaymentSynchronizeService
import beyondeyesight.domain.repository.payment.PaymentRepository
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.*
import java.time.*
import java.util.*
import java.util.stream.Stream
import kotlin.test.Test
import kotlin.test.assertEquals

class GatheringServiceTest {
    private val gatheringRepository: GatheringRepository = mock()
    private val guestService: GuestService = mock()
    private val guestRepository: GuestRepository = mock()
    private val lockService: LockService = mock()
    private val userRepository: UserRepository = mock()
    private val seriesRepository: SeriesRepository = mock()
    private val seriesScheduleRepository: SeriesScheduleRepository = mock()
    private val paymentGateway: PaymentGateway = mock()
    private val paymentService: PaymentService = mock()
    private val paymentRepository: PaymentRepository = mock()
    private val paymentSynchronizeService: PaymentSynchronizeService = mock()

    val gatheringService = GatheringService(
        gatheringRepository = gatheringRepository,
        guestService = guestService,
        guestRepository = guestRepository,
        lockService = lockService,
        userRepository = userRepository,
        seriesRepository = seriesRepository,
        seriesScheduleRepository = seriesScheduleRepository,
        paymentService = paymentService,
        paymentRepository = paymentRepository,
        paymentSynchronizeService = paymentSynchronizeService,
        paymentGateway = paymentGateway,
    )

    @ParameterizedTest(name = "{0}")
    @MethodSource("scheduleFailCases")
    fun scheduleFail(testCase: ScheduleFailCase) {
        // given
        val hostUuid = UUID.randomUUID()
        val userEntity: UserEntity = mock()
        val seriesEntity: SeriesEntity = mock()

        if (testCase.userExists) {
            whenever(userRepository.findByUuid(hostUuid)).thenReturn(userEntity)
            whenever(seriesRepository.save(any<SeriesEntity>())).thenReturn(seriesEntity)
        } else {
            whenever(userRepository.findByUuid(hostUuid)).thenReturn(null)
        }

        // when & then
        val exception = assertThrows<Exception> {
            gatheringService.schedule(
                hostUuid = hostUuid,
                minCapacity = 2,
                maxCapacity = 10,
                genderRatioEnabled = false,
                fee = 10000,
                isSplit = false,
                place = "서울 강남구",
                category = Category.PARTY,
                imageUrl = "https://example.com/image.jpg",
                title = "테스트 모임",
                description = null,
                scheduleType = testCase.scheduleType,
                weeklySchedule = testCase.weeklySchedule,
                dateSchedule = testCase.dateSchedule,
                gatheringDays = 7,
                maxMaleCount = null,
                maxFemaleCount = null
            )
        }

        assertEquals(testCase.expectedExceptionType, exception::class.java)
    }

    data class ScheduleFailCase(
        val name: String,
        val userExists: Boolean,
        val scheduleType: ScheduleType,
        val weeklySchedule: WeeklySchedule?,
        val dateSchedule: DateSchedule?,
        val expectedExceptionType: Class<out Exception>
    ) {
        override fun toString(): String = name
    }

    @Test
    fun openFail_userNotFound() {
        // given
        val hostUuid = UUID.randomUUID()
        whenever(userRepository.findByUuid(hostUuid)).thenReturn(null)

        // when & then
        val exception = assertThrows<ResourceNotFoundException> {
            gatheringService.open(
                hostUuid = hostUuid,
                title = "테스트 모임",
                description = "테스트 모임 설명",
                category = Category.PARTY,
                location = "서울 강남구",
                startDateTime = LocalDateTime.of(2025, 1, 15, 14, 30),
                duration = Duration.ofHours(2),
                minCapacity = 2,
                maxCapacity = 10,
                genderRatioEnabled = false,
                maxMaleCount = null,
                maxFemaleCount = null,
                isFree = false,
                price = 10000,
                isSplit = false,
                imageUrl = "https://example.com/image.jpg",
            )
        }
    }

    @Test
    fun openSucceed() {
        // given
        val hostUuid = UUID.randomUUID()
        val userEntity: UserEntity = mock()
        val savedGatheringEntity: GatheringEntity = mock()

        val startDateTime = LocalDateTime.of(2025, 1, 15, 14, 30)

        whenever(userEntity.uuid).thenReturn(hostUuid)
        whenever(userRepository.findByUuid(hostUuid)).thenReturn(userEntity)
        whenever(gatheringRepository.save(any<GatheringEntity>())).thenReturn(savedGatheringEntity)

        // when
        val result = gatheringService.open(
            hostUuid = hostUuid,
            title = "테스트 모임",
            description = "테스트 모임 설명",
            category = Category.PARTY,
            location = "서울 강남구",
            startDateTime = startDateTime,
            duration = Duration.ofHours(2),
            minCapacity = 2,
            maxCapacity = 10,
            genderRatioEnabled = false,
            maxMaleCount = null,
            maxFemaleCount = null,
            isFree = false,
            price = 10000,
            isSplit = false,
            imageUrl = "https://example.com/image.jpg",
        )

        // then
        verify(userRepository).findByUuid(hostUuid)
        verify(gatheringRepository).save(argThat<GatheringEntity> { gathering ->
            gathering.hostUuid == hostUuid &&
                    gathering.minCapacity == 2 &&
                    gathering.maxCapacity == 10 &&
                    gathering.genderRatioEnabled == false &&
                    gathering.fee == 10000 &&
                    gathering.isSplit == false &&
                    gathering.place == "서울 강남구" &&
                    gathering.category == Category.PARTY &&
                    gathering.imageUrl == "https://example.com/image.jpg" &&
                    gathering.title == "테스트 모임" &&
                    gathering.description == "테스트 모임 설명" &&
                    gathering.startDateTime == startDateTime &&
                    gathering.duration == Duration.ofHours(2) &&
                    gathering.dayOfWeek == startDateTime.dayOfWeek &&
                    gathering.status == Status.OPEN &&
                    gathering.clickCount == GatheringEntity.INITIAL_CLICK_COUNT &&
                    gathering.totalGuests == GatheringEntity.INITIAL_TOTAL_GUESTS &&
                    gathering.score == 0
        })
        assertEquals(savedGatheringEntity, result)
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("joinSuccessCases")
    fun joinSucceed(testCase: JoinSuccessCase) {
        // given
        val gatheringUuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val userEntity: UserEntity = mock()
        val gatheringEntity: GatheringEntity = mock()
        val lockToken = "lock-token"

        whenever(userRepository.findByUuid(userUuid)).thenReturn(userEntity)
        whenever(lockService.lockWithRetry(
            eq("gathering"),
            eq(gatheringUuid.toString()),
            any(),
            any(),
            any()
        )).thenReturn(lockToken)
        whenever(gatheringRepository.findByUuid(gatheringUuid)).thenReturn(gatheringEntity)
        whenever(gatheringEntity.uuid).thenReturn(gatheringUuid)
        whenever(gatheringEntity.maxCapacity).thenReturn(testCase.maxCapacity)
        whenever(gatheringEntity.genderRatioEnabled).thenReturn(testCase.genderRatioEnabled)
        whenever(gatheringEntity.maxMaleCount).thenReturn(testCase.maxMaleCount)
        whenever(gatheringEntity.maxFemaleCount).thenReturn(testCase.maxFemaleCount)
        whenever(gatheringEntity.isFree()).thenReturn(true)
        whenever(guestRepository.countByGathering(gatheringUuid)).thenReturn(testCase.currentGuestCount.toLong())
        whenever(guestRepository.countByGatheringAndGender(gatheringUuid, Gender.MALE)).thenReturn(testCase.currentMaleCount.toLong())
        whenever(guestRepository.countByGatheringAndGender(gatheringUuid, Gender.FEMALE)).thenReturn(testCase.currentFemaleCount.toLong())

        // when
        gatheringService.join(gatheringUuid, userUuid, confirmPaymentRequest = null)

        // then
        verify(userRepository).findByUuid(userUuid)
        verify(lockService).lockWithRetry(
            eq("gathering"),
            eq(gatheringUuid.toString()),
            any(),
            any(),
            any()
        )
        verify(gatheringRepository).findByUuid(gatheringUuid)
        verify(guestRepository).countByGathering(gatheringUuid)

        if (testCase.genderRatioEnabled) {
            verify(guestRepository).countByGatheringAndGender(gatheringUuid, Gender.MALE)
            verify(guestRepository).countByGatheringAndGender(gatheringUuid, Gender.FEMALE)
        }

        verify(guestService).join(gatheringUuid, userUuid)
        verify(lockService).unlock("gathering", gatheringUuid.toString(), lockToken)
    }

    data class JoinSuccessCase(
        val name: String,
        val maxCapacity: Int,
        val currentGuestCount: Int,
        val genderRatioEnabled: Boolean,
        val maxMaleCount: Int?,
        val maxFemaleCount: Int?,
        val currentMaleCount: Int,
        val currentFemaleCount: Int
    ) {
        override fun toString(): String = name
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("joinFailCases")
    fun joinFail(testCase: JoinFailCase) {
        // given
        val gatheringUuid = UUID.randomUUID()
        val userUuid = UUID.randomUUID()
        val userEntity: UserEntity = mock()
        val gatheringEntity: GatheringEntity = mock()
        val lockToken = "lock-token"

        if (testCase.userExists) {
            whenever(userRepository.findByUuid(userUuid)).thenReturn(userEntity)
        } else {
            whenever(userRepository.findByUuid(userUuid)).thenReturn(null)
        }

        if (testCase.lockAcquired) {
            whenever(lockService.lockWithRetry(
                eq("gathering"),
                eq(gatheringUuid.toString()),
                any(),
                any(),
                any()
            )).thenReturn(lockToken)
        } else {
            whenever(lockService.lockWithRetry(
                eq("gathering"),
                eq(gatheringUuid.toString()),
                any(),
                any(),
                any()
            )).thenReturn(null)
        }

        if (testCase.gatheringExists) {
            whenever(gatheringRepository.findByUuid(gatheringUuid)).thenReturn(gatheringEntity)
            whenever(gatheringEntity.uuid).thenReturn(gatheringUuid)
            whenever(gatheringEntity.maxCapacity).thenReturn(testCase.maxCapacity)
            whenever(gatheringEntity.genderRatioEnabled).thenReturn(testCase.genderRatioEnabled)
            whenever(gatheringEntity.maxMaleCount).thenReturn(testCase.maxMaleCount)
            whenever(gatheringEntity.maxFemaleCount).thenReturn(testCase.maxFemaleCount)
            whenever(gatheringEntity.isFree()).thenReturn(true)
            whenever(guestRepository.countByGathering(gatheringUuid)).thenReturn(testCase.currentGuestCount.toLong())
            whenever(guestRepository.countByGatheringAndGender(gatheringUuid, Gender.MALE)).thenReturn(testCase.currentMaleCount.toLong())
            whenever(guestRepository.countByGatheringAndGender(gatheringUuid, Gender.FEMALE)).thenReturn(testCase.currentFemaleCount.toLong())
        } else {
            whenever(gatheringRepository.findByUuid(gatheringUuid)).thenReturn(null)
        }

        // when & then
        val exception = assertThrows<Exception> {
            gatheringService.join(gatheringUuid, userUuid, confirmPaymentRequest = null)
        }

        assertEquals(testCase.expectedExceptionType, exception::class.java)

        // verify unlock is called if lock was acquired
        if (testCase.lockAcquired && testCase.userExists) {
            verify(lockService).unlock("gathering", gatheringUuid.toString(), lockToken)
        }

        // verify guestService.join is never called on failure
        verify(guestService, never()).join(any(), any())
    }

    data class JoinFailCase(
        val name: String,
        val userExists: Boolean,
        val lockAcquired: Boolean,
        val gatheringExists: Boolean,
        val maxCapacity: Int,
        val currentGuestCount: Int,
        val genderRatioEnabled: Boolean,
        val maxMaleCount: Int?,
        val maxFemaleCount: Int?,
        val currentMaleCount: Int,
        val currentFemaleCount: Int,
        val expectedExceptionType: Class<out Exception>
    ) {
        override fun toString(): String = name
    }

    @Test
    fun scheduleSucceed() {
        // given
        val hostUuid: UUID = mock()
        val userEntity: UserEntity = mock()
        val seriesEntity: SeriesEntity = mock()
        val seriesScheduleEntity: SeriesScheduleEntity = mock()

        val startDate = LocalDate.of(2025, 1, 15)
        val startTime = LocalTime.of(14, 30)
        val gatheringDays = 7

        whenever(userRepository.findByUuid(hostUuid)).thenReturn(userEntity)
        whenever(seriesRepository.save(any<SeriesEntity>())).thenReturn(seriesEntity)
        whenever(seriesScheduleRepository.save(any<SeriesScheduleEntity>())).thenReturn(seriesScheduleEntity)

        // when
        gatheringService.schedule(
            hostUuid = hostUuid,
            minCapacity = 2,
            maxCapacity = 10,
            genderRatioEnabled = false,
            fee = 10000,
            isSplit = false,
            place = "서울 강남구",
            category = Category.PARTY,
            imageUrl = "https://example.com/image.jpg",
            title = "테스트 모임",
            description = null,
            scheduleType = ScheduleType.DATE,
            weeklySchedule = null,
            dateSchedule = DateSchedule(
                summaries = listOf(
                    DateSchedule.DateScheduleSummary(
                        startDate = startDate,
                        startTime = startTime,
                        duration = Duration.ofHours(2)
                    )
                )
            ),
            gatheringDays = gatheringDays,
            maxMaleCount = null,
            maxFemaleCount = null
        )

        // then
        verify(userRepository).findByUuid(hostUuid)
        verify(seriesRepository).save(any<SeriesEntity>())
        verify(seriesScheduleRepository, times(1)).save(argThat<SeriesScheduleEntity> { schedule ->
            schedule.scheduleType == ScheduleType.DATE &&
                    schedule.startDate == startDate &&
                    schedule.startTime == startTime &&
                    schedule.openDate == startDate.minusDays(gatheringDays.toLong()) &&
                    schedule.seriesEntity == seriesEntity
        })
    }

    companion object {
        private val validWeeklySchedule = WeeklySchedule(
            startDate = LocalDate.of(2025, 1, 1),
            endDate = LocalDate.of(2025, 12, 31),
            summaries = listOf(
                WeeklySchedule.WeeklyScheduleSummary(
                    startDayOfWeek = DayOfWeek.MONDAY,
                    startTime = LocalTime.of(14, 30),
                    duration = Duration.ofHours(2)
                )
            )
        )

        private val validDateSchedule = DateSchedule(
            summaries = listOf(
                DateSchedule.DateScheduleSummary(
                    startDate = LocalDate.of(2025, 1, 15),
                    startTime = LocalTime.of(14, 30),
                    duration = Duration.ofHours(2)
                )
            )
        )

        @JvmStatic
        fun scheduleFailCases(): Stream<ScheduleFailCase> = Stream.of(
            ScheduleFailCase(
                name = "User not found",
                userExists = false,
                scheduleType = ScheduleType.DATE,
                weeklySchedule = null,
                dateSchedule = validDateSchedule,
                expectedExceptionType = ResourceNotFoundException::class.java
            ),
            ScheduleFailCase(
                name = "WEEKLY: weeklySchedule is null",
                userExists = true,
                scheduleType = ScheduleType.WEEKLY,
                weeklySchedule = null,
                dateSchedule = null,
                expectedExceptionType = InvalidValueException::class.java
            ),
            ScheduleFailCase(
                name = "WEEKLY: weeklySchedule.summaries is empty",
                userExists = true,
                scheduleType = ScheduleType.WEEKLY,
                weeklySchedule = WeeklySchedule(
                    startDate = LocalDate.of(2025, 1, 1),
                    endDate = LocalDate.of(2025, 12, 31),
                    summaries = emptyList()
                ),
                dateSchedule = null,
                expectedExceptionType = InvalidValueException::class.java
            ),
            ScheduleFailCase(
                name = "WEEKLY: dateSchedule is not null",
                userExists = true,
                scheduleType = ScheduleType.WEEKLY,
                weeklySchedule = validWeeklySchedule,
                dateSchedule = validDateSchedule,
                expectedExceptionType = InvalidValueException::class.java
            ),
            ScheduleFailCase(
                name = "WEEKLY: startTime is not 30-minute interval",
                userExists = true,
                scheduleType = ScheduleType.WEEKLY,
                weeklySchedule = WeeklySchedule(
                    startDate = LocalDate.of(2025, 1, 1),
                    endDate = LocalDate.of(2025, 12, 31),
                    summaries = listOf(
                        WeeklySchedule.WeeklyScheduleSummary(
                            startDayOfWeek = DayOfWeek.MONDAY,
                            startTime = LocalTime.of(14, 15),
                            duration = Duration.ofHours(2)
                        )
                    )
                ),
                dateSchedule = null,
                expectedExceptionType = InvalidValueException::class.java
            ),
            ScheduleFailCase(
                name = "DATE: dateSchedule is null",
                userExists = true,
                scheduleType = ScheduleType.DATE,
                weeklySchedule = null,
                dateSchedule = null,
                expectedExceptionType = InvalidValueException::class.java
            ),
            ScheduleFailCase(
                name = "DATE: dateSchedule.summaries is empty",
                userExists = true,
                scheduleType = ScheduleType.DATE,
                weeklySchedule = null,
                dateSchedule = DateSchedule(summaries = emptyList()),
                expectedExceptionType = InvalidValueException::class.java
            ),
            ScheduleFailCase(
                name = "DATE: weeklySchedule is not null",
                userExists = true,
                scheduleType = ScheduleType.DATE,
                weeklySchedule = validWeeklySchedule,
                dateSchedule = validDateSchedule,
                expectedExceptionType = InvalidValueException::class.java
            ),
            ScheduleFailCase(
                name = "DATE: startTime is not 30-minute interval",
                userExists = true,
                scheduleType = ScheduleType.DATE,
                weeklySchedule = null,
                dateSchedule = DateSchedule(
                    summaries = listOf(
                        DateSchedule.DateScheduleSummary(
                            startDate = LocalDate.of(2025, 1, 15),
                            startTime = LocalTime.of(14, 45),
                            duration = Duration.ofHours(2)
                        )
                    )
                ),
                expectedExceptionType = InvalidValueException::class.java
            )
        )

        @JvmStatic
        fun joinSuccessCases(): Stream<JoinSuccessCase> = Stream.of(
            JoinSuccessCase(
                name = "Without gender ratio",
                maxCapacity = 10,
                currentGuestCount = 5,
                genderRatioEnabled = false,
                maxMaleCount = null,
                maxFemaleCount = null,
                currentMaleCount = 0,
                currentFemaleCount = 0
            ),
            JoinSuccessCase(
                name = "With gender ratio",
                maxCapacity = 10,
                currentGuestCount = 4,
                genderRatioEnabled = true,
                maxMaleCount = 5,
                maxFemaleCount = 5,
                currentMaleCount = 2,
                currentFemaleCount = 2
            ),
            JoinSuccessCase(
                name = "At max capacity boundary",
                maxCapacity = 10,
                currentGuestCount = 9,
                genderRatioEnabled = false,
                maxMaleCount = null,
                maxFemaleCount = null,
                currentMaleCount = 0,
                currentFemaleCount = 0
            ),
            JoinSuccessCase(
                name = "At male capacity boundary",
                maxCapacity = 10,
                currentGuestCount = 6,
                genderRatioEnabled = true,
                maxMaleCount = 5,
                maxFemaleCount = 5,
                currentMaleCount = 4,
                currentFemaleCount = 2
            ),
            JoinSuccessCase(
                name = "At female capacity boundary",
                maxCapacity = 10,
                currentGuestCount = 6,
                genderRatioEnabled = true,
                maxMaleCount = 5,
                maxFemaleCount = 5,
                currentMaleCount = 2,
                currentFemaleCount = 4
            )
        )

        @JvmStatic
        fun joinFailCases(): Stream<JoinFailCase> = Stream.of(
            JoinFailCase(
                name = "User not found",
                userExists = false,
                lockAcquired = false,
                gatheringExists = false,
                maxCapacity = 10,
                currentGuestCount = 0,
                genderRatioEnabled = false,
                maxMaleCount = null,
                maxFemaleCount = null,
                currentMaleCount = 0,
                currentFemaleCount = 0,
                expectedExceptionType = ResourceNotFoundException::class.java
            ),
            JoinFailCase(
                name = "Failed to acquire lock",
                userExists = true,
                lockAcquired = false,
                gatheringExists = false,
                maxCapacity = 10,
                currentGuestCount = 0,
                genderRatioEnabled = false,
                maxMaleCount = null,
                maxFemaleCount = null,
                currentMaleCount = 0,
                currentFemaleCount = 0,
                expectedExceptionType = LockAcquireFailException::class.java
            ),
            JoinFailCase(
                name = "Gathering not found",
                userExists = true,
                lockAcquired = true,
                gatheringExists = false,
                maxCapacity = 10,
                currentGuestCount = 0,
                genderRatioEnabled = false,
                maxMaleCount = null,
                maxFemaleCount = null,
                currentMaleCount = 0,
                currentFemaleCount = 0,
                expectedExceptionType = ResourceNotFoundException::class.java
            ),
            JoinFailCase(
                name = "Gathering is full (total capacity exceeded)",
                userExists = true,
                lockAcquired = true,
                gatheringExists = true,
                maxCapacity = 10,
                currentGuestCount = 10,
                genderRatioEnabled = false,
                maxMaleCount = null,
                maxFemaleCount = null,
                currentMaleCount = 0,
                currentFemaleCount = 0,
                expectedExceptionType = CannotJoinException::class.java
            ),
            JoinFailCase(
                name = "DataIntegrity: maxMaleCount is null when genderRatioEnabled",
                userExists = true,
                lockAcquired = true,
                gatheringExists = true,
                maxCapacity = 10,
                currentGuestCount = 4,
                genderRatioEnabled = true,
                maxMaleCount = null,
                maxFemaleCount = 5,
                currentMaleCount = 2,
                currentFemaleCount = 2,
                expectedExceptionType = DataIntegrityException::class.java
            ),
            JoinFailCase(
                name = "Male capacity exceeded",
                userExists = true,
                lockAcquired = true,
                gatheringExists = true,
                maxCapacity = 10,
                currentGuestCount = 7,
                genderRatioEnabled = true,
                maxMaleCount = 5,
                maxFemaleCount = 5,
                currentMaleCount = 5,
                currentFemaleCount = 2,
                expectedExceptionType = CannotJoinException::class.java
            ),
            JoinFailCase(
                name = "DataIntegrity: maxFemaleCount is null when genderRatioEnabled",
                userExists = true,
                lockAcquired = true,
                gatheringExists = true,
                maxCapacity = 10,
                currentGuestCount = 4,
                genderRatioEnabled = true,
                maxMaleCount = 5,
                maxFemaleCount = null,
                currentMaleCount = 2,
                currentFemaleCount = 2,
                expectedExceptionType = DataIntegrityException::class.java
            ),
            JoinFailCase(
                name = "Female capacity exceeded",
                userExists = true,
                lockAcquired = true,
                gatheringExists = true,
                maxCapacity = 10,
                currentGuestCount = 7,
                genderRatioEnabled = true,
                maxMaleCount = 5,
                maxFemaleCount = 5,
                currentMaleCount = 2,
                currentFemaleCount = 5,
                expectedExceptionType = CannotJoinException::class.java
            )
        )
    }
}
