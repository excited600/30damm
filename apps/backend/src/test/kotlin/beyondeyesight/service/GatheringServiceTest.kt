package beyondeyesight.service

import beyondeyesight.domain.exception.InvalidValueException
import beyondeyesight.domain.exception.ResourceNotFoundException
import beyondeyesight.domain.model.User.UserEntity
import beyondeyesight.domain.model.gathering.Category
import beyondeyesight.domain.model.gathering.DateSchedule
import beyondeyesight.domain.model.gathering.GatheringEntity
import beyondeyesight.domain.model.gathering.ScheduleType
import beyondeyesight.domain.model.gathering.SeriesEntity
import beyondeyesight.domain.model.gathering.SeriesScheduleEntity
import beyondeyesight.domain.model.gathering.SubCategory
import beyondeyesight.domain.model.gathering.WeeklySchedule
import beyondeyesight.domain.repository.GuestRepository
import beyondeyesight.domain.repository.UserRepository
import beyondeyesight.domain.repository.gathering.GatheringRepository
import beyondeyesight.domain.repository.gathering.SeriesRepository
import beyondeyesight.domain.repository.gathering.SeriesScheduleRepository
import beyondeyesight.domain.service.LockService
import beyondeyesight.domain.service.PayService
import beyondeyesight.domain.service.gathering.GatheringService
import beyondeyesight.domain.service.gathering.GuestService
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
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
    private val payService: PayService = mock()

    val gatheringService = GatheringService(
        gatheringRepository = gatheringRepository,
        guestService = guestService,
        guestRepository = guestRepository,
        lockService = lockService,
        userRepository = userRepository,
        seriesRepository = seriesRepository,
        seriesScheduleRepository = seriesScheduleRepository,
        payService = payService,
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
    }

}