package beyondeyesight.service

import beyondeyesight.domain.model.User.UserEntity
import beyondeyesight.domain.model.gathering.Category
import beyondeyesight.domain.model.gathering.DateSchedule
import beyondeyesight.domain.model.gathering.GatheringEntity
import beyondeyesight.domain.model.gathering.ScheduleType
import beyondeyesight.domain.model.gathering.SeriesEntity
import beyondeyesight.domain.model.gathering.SeriesScheduleEntity
import beyondeyesight.domain.model.gathering.SubCategory
import beyondeyesight.domain.repository.GuestRepository
import beyondeyesight.domain.repository.UserRepository
import beyondeyesight.domain.repository.gathering.GatheringRepository
import beyondeyesight.domain.repository.gathering.SeriesRepository
import beyondeyesight.domain.repository.gathering.SeriesScheduleRepository
import beyondeyesight.domain.service.LockService
import beyondeyesight.domain.service.PayService
import beyondeyesight.domain.service.gathering.GatheringService
import beyondeyesight.domain.service.gathering.GuestService
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import kotlin.test.Test

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

    @Test
    fun schedule() {
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

}