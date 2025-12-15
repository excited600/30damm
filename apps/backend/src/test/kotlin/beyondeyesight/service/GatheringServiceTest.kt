package beyondeyesight.service

import beyondeyesight.domain.exception.DataIntegrityException
import beyondeyesight.domain.exception.InvalidValueException
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

    val gatheringService = GatheringService(
        gatheringRepository = gatheringRepository,
        guestService = guestService,
        guestRepository = guestRepository,
        lockService = lockService,
        userRepository = userRepository,
        seriesRepository = seriesRepository,
        seriesScheduleRepository = seriesScheduleRepository,
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
                approveType = GatheringEntity.ApproveType.FIRST_IN,
                minCapacity = 2,
                maxCapacity = 10,
                genderRatioEnabled = false,
                minAge = 30,
                maxAge = 40,
                fee = 10000,
                discountEnabled = false,
                offline = true,
                place = "서울 강남구",
                category = Category.PARTY,
                subCategory = SubCategory.HOME_PARTY,
                imageUrl = "https://example.com/image.jpg",
                title = "테스트 모임",
                introduction = "테스트 모임 소개",
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("openFailCases")
    fun openFail(testCase: OpenFailCase) {
        // given
        val hostUuid = UUID.randomUUID()
        val userEntity: UserEntity = mock()

        if (testCase.userExists) {
            whenever(userEntity.uuid).thenReturn(hostUuid)
            whenever(userRepository.findByUuid(hostUuid)).thenReturn(userEntity)
        } else {
            whenever(userRepository.findByUuid(hostUuid)).thenReturn(null)
        }

        // when & then
        val exception = assertThrows<Exception> {
            gatheringService.open(
                hostUuid = hostUuid,
                approveType = GatheringEntity.ApproveType.FIRST_IN,
                minCapacity = 2,
                maxCapacity = 10,
                genderRatioEnabled = false,
                minAge = testCase.minAge,
                maxAge = testCase.maxAge,
                maxMaleCount = testCase.maxMaleCount,
                maxFemaleCount = testCase.maxFemaleCount,
                fee = testCase.fee,
                discountEnabled = false,
                offline = true,
                place = "서울 강남구",
                category = Category.PARTY,
                subCategory = SubCategory.HOME_PARTY,
                imageUrl = "https://example.com/image.jpg",
                title = "테스트 모임",
                introduction = "테스트 모임 소개",
                startDateTime = LocalDateTime.of(2025, 1, 15, 14, 30),
                duration = Duration.ofHours(2)
            )
        }

        assertEquals(testCase.expectedExceptionType, exception::class.java)
    }

    data class OpenFailCase(
        val name: String,
        val userExists: Boolean,
        val minAge: Int,
        val maxAge: Int,
        val maxMaleCount: Int?,
        val maxFemaleCount: Int?,
        val fee: Int,
        val expectedExceptionType: Class<out Exception>
    ) {
        override fun toString(): String = name
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
            approveType = GatheringEntity.ApproveType.FIRST_IN,
            minCapacity = 2,
            maxCapacity = 10,
            genderRatioEnabled = false,
            minAge = 30,
            maxAge = 40,
            maxMaleCount = null,
            maxFemaleCount = null,
            fee = 10000,
            discountEnabled = false,
            offline = true,
            place = "서울 강남구",
            category = Category.PARTY,
            subCategory = SubCategory.HOME_PARTY,
            imageUrl = "https://example.com/image.jpg",
            title = "테스트 모임",
            introduction = "테스트 모임 소개",
            startDateTime = startDateTime,
            duration = Duration.ofHours(2)
        )

        // then
        verify(userRepository).findByUuid(hostUuid)
        verify(gatheringRepository).save(argThat<GatheringEntity> { gathering ->
            gathering.hostUuid == hostUuid &&
                    gathering.approveType == GatheringEntity.ApproveType.FIRST_IN &&
                    gathering.minCapacity == 2 &&
                    gathering.maxCapacity == 10 &&
                    gathering.genderRatioEnabled == false &&
                    gathering.minAge == 30 &&
                    gathering.maxAge == 40 &&
                    gathering.fee == 10000 &&
                    gathering.discountEnabled == false &&
                    gathering.offline == true &&
                    gathering.place == "서울 강남구" &&
                    gathering.category == Category.PARTY &&
                    gathering.subCategory == SubCategory.HOME_PARTY &&
                    gathering.imageUrl == "https://example.com/image.jpg" &&
                    gathering.title == "테스트 모임" &&
                    gathering.introduction == "테스트 모임 소개" &&
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
        whenever(guestRepository.countByGathering(gatheringUuid)).thenReturn(testCase.currentGuestCount.toLong())
        whenever(guestRepository.countByGatheringAndGender(gatheringUuid, Gender.M)).thenReturn(testCase.currentMaleCount.toLong())
        whenever(guestRepository.countByGatheringAndGender(gatheringUuid, Gender.F)).thenReturn(testCase.currentFemaleCount.toLong())

        // when
        gatheringService.join(gatheringUuid, userUuid)

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
            verify(guestRepository).countByGatheringAndGender(gatheringUuid, Gender.M)
            verify(guestRepository).countByGatheringAndGender(gatheringUuid, Gender.F)
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
            whenever(guestRepository.countByGathering(gatheringUuid)).thenReturn(testCase.currentGuestCount.toLong())
            whenever(guestRepository.countByGatheringAndGender(gatheringUuid, Gender.M)).thenReturn(testCase.currentMaleCount.toLong())
            whenever(guestRepository.countByGatheringAndGender(gatheringUuid, Gender.F)).thenReturn(testCase.currentFemaleCount.toLong())
        } else {
            whenever(gatheringRepository.findByUuid(gatheringUuid)).thenReturn(null)
        }

        // when & then
        val exception = assertThrows<Exception> {
            gatheringService.join(gatheringUuid, userUuid)
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
            approveType = GatheringEntity.ApproveType.FIRST_IN,
            minCapacity = 2,
            maxCapacity = 10,
            genderRatioEnabled = false,
            minAge = 30,
            maxAge = 40,
            fee = 10000,
            discountEnabled = false,
            offline = true,
            place = "서울 강남구",
            category = Category.PARTY,
            subCategory = SubCategory.HOME_PARTY,
            imageUrl = "https://example.com/image.jpg",
            title = "테스트 모임",
            introduction = "테스트 모임 소개",
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
                    startTime = LocalTime.of(14, 30), // 30분 간격
                    duration = Duration.ofHours(2)
                )
            )
        )

        private val validDateSchedule = DateSchedule(
            summaries = listOf(
                DateSchedule.DateScheduleSummary(
                    startDate = LocalDate.of(2025, 1, 15),
                    startTime = LocalTime.of(14, 30), // 30분 간격
                    duration = Duration.ofHours(2)
                )
            )
        )

        @JvmStatic
        fun scheduleFailCases(): Stream<ScheduleFailCase> = Stream.of(
            // 1. User not found
            ScheduleFailCase(
                name = "User not found",
                userExists = false,
                scheduleType = ScheduleType.DATE,
                weeklySchedule = null,
                dateSchedule = validDateSchedule,
                expectedExceptionType = ResourceNotFoundException::class.java
            ),

            // WEEKLY 케이스들
            // 2. WEEKLY: weeklySchedule이 null
            ScheduleFailCase(
                name = "WEEKLY: weeklySchedule is null",
                userExists = true,
                scheduleType = ScheduleType.WEEKLY,
                weeklySchedule = null,
                dateSchedule = null,
                expectedExceptionType = InvalidValueException::class.java
            ),

            // 3. WEEKLY: weeklySchedule.summaries가 빈 리스트
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

            // 4. WEEKLY: dateSchedule이 not null
            ScheduleFailCase(
                name = "WEEKLY: dateSchedule is not null",
                userExists = true,
                scheduleType = ScheduleType.WEEKLY,
                weeklySchedule = validWeeklySchedule,
                dateSchedule = validDateSchedule,
                expectedExceptionType = InvalidValueException::class.java
            ),

            // 5. WEEKLY: startTime이 30분 간격이 아닐 때
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
                            startTime = LocalTime.of(14, 15), // 30분 간격 아님
                            duration = Duration.ofHours(2)
                        )
                    )
                ),
                dateSchedule = null,
                expectedExceptionType = InvalidValueException::class.java
            ),

            // DATE 케이스들
            // 6. DATE: dateSchedule이 null
            ScheduleFailCase(
                name = "DATE: dateSchedule is null",
                userExists = true,
                scheduleType = ScheduleType.DATE,
                weeklySchedule = null,
                dateSchedule = null,
                expectedExceptionType = InvalidValueException::class.java
            ),

            // 7. DATE: dateSchedule.summaries가 빈 리스트
            ScheduleFailCase(
                name = "DATE: dateSchedule.summaries is empty",
                userExists = true,
                scheduleType = ScheduleType.DATE,
                weeklySchedule = null,
                dateSchedule = DateSchedule(summaries = emptyList()),
                expectedExceptionType = InvalidValueException::class.java
            ),

            // 8. DATE: weeklySchedule이 not null
            ScheduleFailCase(
                name = "DATE: weeklySchedule is not null",
                userExists = true,
                scheduleType = ScheduleType.DATE,
                weeklySchedule = validWeeklySchedule,
                dateSchedule = validDateSchedule,
                expectedExceptionType = InvalidValueException::class.java
            ),

            // 9. DATE: startTime이 30분 간격이 아닐 때
            ScheduleFailCase(
                name = "DATE: startTime is not 30-minute interval",
                userExists = true,
                scheduleType = ScheduleType.DATE,
                weeklySchedule = null,
                dateSchedule = DateSchedule(
                    summaries = listOf(
                        DateSchedule.DateScheduleSummary(
                            startDate = LocalDate.of(2025, 1, 15),
                            startTime = LocalTime.of(14, 45), // 30분 간격 아님
                            duration = Duration.ofHours(2)
                        )
                    )
                ),
                expectedExceptionType = InvalidValueException::class.java
            )
        )

        @JvmStatic
        fun openFailCases(): Stream<OpenFailCase> = Stream.of(
            // 1. User not found
            OpenFailCase(
                name = "User not found",
                userExists = false,
                minAge = 30,
                maxAge = 40,
                maxMaleCount = null,
                maxFemaleCount = null,
                fee = 10000,
                expectedExceptionType = ResourceNotFoundException::class.java
            ),

            // 2. minAge < 1
            OpenFailCase(
                name = "minAge is less than 1",
                userExists = true,
                minAge = 0,
                maxAge = 40,
                maxMaleCount = null,
                maxFemaleCount = null,
                fee = 10000,
                expectedExceptionType = InvalidValueException::class.java
            ),

            // 4. maxAge < minAge
            OpenFailCase(
                name = "maxAge is less than minAge",
                userExists = true,
                minAge = 30,
                maxAge = 29,
                maxMaleCount = null,
                maxFemaleCount = null,
                fee = 10000,
                expectedExceptionType = InvalidValueException::class.java
            ),

            // 5. maxMaleCount < 0
            OpenFailCase(
                name = "maxMaleCount is negative",
                userExists = true,
                minAge = 30,
                maxAge = 40,
                maxMaleCount = -1,
                maxFemaleCount = null,
                fee = 10000,
                expectedExceptionType = InvalidValueException::class.java
            ),

            // 6. maxFemaleCount < 0
            OpenFailCase(
                name = "maxFemaleCount is negative",
                userExists = true,
                minAge = 30,
                maxAge = 40,
                maxMaleCount = null,
                maxFemaleCount = -1,
                fee = 10000,
                expectedExceptionType = InvalidValueException::class.java
            ),

            // 7. fee % 1000 != 0 (500원)
            OpenFailCase(
                name = "fee is not multiple of 1000 (500)",
                userExists = true,
                minAge = 30,
                maxAge = 40,
                maxMaleCount = null,
                maxFemaleCount = null,
                fee = 500,
                expectedExceptionType = InvalidValueException::class.java
            ),

            // 8. fee % 1000 != 0 (10001원)
            OpenFailCase(
                name = "fee is not multiple of 1000 (10001)",
                userExists = true,
                minAge = 30,
                maxAge = 40,
                maxMaleCount = null,
                maxFemaleCount = null,
                fee = 10001,
                expectedExceptionType = InvalidValueException::class.java
            )
        )

        @JvmStatic
        fun joinSuccessCases(): Stream<JoinSuccessCase> = Stream.of(
            // 1. 성별 비율 미적용
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

            // 2. 성별 비율 적용
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

            // 3. 최대 인원 직전 (경계값)
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

            // 4. 남성 최대 인원 직전 (경계값)
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

            // 5. 여성 최대 인원 직전 (경계값)
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
            // 1. User not found
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

            // 2. Lock 획득 실패
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
                expectedExceptionType = IllegalStateException::class.java
            ),

            // 3. Gathering not found
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

            // 4. 총 인원 초과 (currentGuestCount + 1 > maxCapacity)
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

            // 5. genderRatioEnabled = true & maxMaleCount가 null (DataIntegrityException)
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

            // 6. genderRatioEnabled = true & 남성 인원 초과
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

            // 7. genderRatioEnabled = true & maxFemaleCount가 null (DataIntegrityException)
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

            // 8. genderRatioEnabled = true & 여성 인원 초과
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