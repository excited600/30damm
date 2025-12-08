package beyondeyesight.integration

import beyondeyesight.domain.model.UserEntity
import beyondeyesight.model.GatheringApproveType
import beyondeyesight.model.GatheringCategory
import beyondeyesight.model.GatheringDayOfWeek
import beyondeyesight.model.GatheringScheduleType
import beyondeyesight.model.GatheringSubCategory
import beyondeyesight.model.GatheringWeeklyScheduleSummary
import beyondeyesight.model.OpenGatheringRequest
import beyondeyesight.model.OpenGatheringResponse
import beyondeyesight.model.WeeklyScheduleSeriesRequest
import beyondeyesight.ui.UserController
import org.assertj.core.api.Assertions.assertThat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.test.Test

class GatheringControllerTest : EndToEndTestBase() {

    @Test
    fun `Schedule Series`() {
        val signUpRequest = UserController.SignUpRequest(
            email = "email",
            nickname = "nickname",
            age = 25,
            gender = UserEntity.Gender.M,
            introduction = "intro",
            password = "password",
            phoneNumber = "01012345671",
            phoneAuthenticated = true
        )

        val host = webTestClient.post()
            .uri("/users/")
            .bodyValue(signUpRequest)
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody(UserController.SignUpResponse::class.java)
            .returnResult()
            .responseBody!!

        val anyString = "string"
        val minFee = 1_000

        val request = WeeklyScheduleSeriesRequest(
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(7),
            summaries = arrayListOf(
                GatheringWeeklyScheduleSummary(
                    startDayOfWeek = GatheringDayOfWeek.MONDAY,
                    startTime = LocalTime.of(1, 0),
                    duration = 2f
                )
            ),
            scheduleType = GatheringScheduleType.WEEKLY,
            gatheringDays = 7,
            hostUuid = host.uuid,
            approveType = GatheringApproveType.FIRST_IN,
            minCapacity = 1,
            maxCapacity = 10,
            genderRatioEnabled = true,
            minAge = 20,
            maxAge = 40,
            fee = minFee,
            discountEnabled = false,
            offline = true,
            place = anyString,
            category = GatheringCategory.ACTIVITY,
            subCategory = GatheringSubCategory.HOME_PARTY,
            imageUrl = anyString,
            title = anyString,
            introduction = anyString,
            maxMaleCount = 5,
            maxFemaleCount = 5
        )

        webTestClient.post()
            .uri("/series")
            .bodyValue(request)
            .exchange()
            .expectStatus().is2xxSuccessful

        val seriesCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM series",
            Long::class.java
        )
        val seriesScheduleCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM series_schedules",
            Long::class.java
        )

        assertThat(seriesCount).isEqualTo(1L)
        assertThat(seriesScheduleCount).isEqualTo(1L)

    }

    @Test
    fun `Open Gathering`() {
        val signUpRequest = UserController.SignUpRequest(
            email = "email",
            nickname = "nickname",
            age = 25,
            gender = UserEntity.Gender.M,
            introduction = "intro",
            password = "password",
            phoneNumber = "01012345671",
            phoneAuthenticated = true
        )

        val host = webTestClient.post()
            .uri("/users/")
            .bodyValue(signUpRequest)
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody(UserController.SignUpResponse::class.java)
            .returnResult()
            .responseBody!!

        val anyInt = 1
        val anyString = "string"
        val minFee = 1_000
        val request = OpenGatheringRequest(
            hostUuid = host.uuid,
            approveType = GatheringApproveType.FIRST_IN,
            minCapacity = anyInt,
            maxCapacity = anyInt + 1,
            genderRatioEnabled = false,
            minAge = anyInt,
            maxAge = anyInt + 1,
            fee = minFee,
            discountEnabled = false,
            offline = true,
            place = anyString,
            category = GatheringCategory.ACTIVITY,
            subCategory = GatheringSubCategory.HOME_PARTY,
            imageUrl = anyString,
            title = anyString,
            introduction = anyString,
            startDateTime = LocalDateTime.now().plusDays(7),
            maxMaleCount = null,
            maxFemaleCount = null,
            duration = 2.5f
        )

        webTestClient.post()
            .uri("/gatherings")
            .bodyValue(request)
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody(OpenGatheringResponse::class.java)

        val count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM gatherings",
            Long::class.java
        )

        assertThat(count).isEqualTo(1L)

//        객체로 변환하는 예제 코드
//        val map = jdbcTemplate.queryForMap(
//            "SELECT COUNT(*) FROM gatherings",
//        )
//        val gathering = objectMapper.convertValue(map, GatheringEntity::class.java)


    }
}