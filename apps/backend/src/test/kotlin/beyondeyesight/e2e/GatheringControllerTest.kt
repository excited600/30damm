package beyondeyesight.e2e

import beyondeyesight.domain.model.user.Gender
import beyondeyesight.model.GatheringApproveType
import beyondeyesight.model.GatheringCategory
import beyondeyesight.model.GatheringDayOfWeek
import beyondeyesight.model.GatheringScheduleType
import beyondeyesight.model.GatheringSubCategory
import beyondeyesight.model.GatheringWeeklyScheduleSummary
import beyondeyesight.model.JoinGatheringRequest
import beyondeyesight.model.OpenGatheringRequest
import beyondeyesight.model.PreparePaymentRequest
import beyondeyesight.model.PreparePaymentResponse
import beyondeyesight.model.ProductType
import beyondeyesight.model.ConfirmPaymentRequest
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
            gender = Gender.M,
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
            gender = Gender.M,
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

    }

    @Test
    fun `무료 모임 참여`() {
        //given
        // 호스트 생성
        val hostSignUpRequest = UserController.SignUpRequest(
            email = "host@email.com",
            nickname = "host",
            age = 30,
            gender = Gender.M,
            introduction = "host intro",
            password = "password",
            phoneNumber = "01012345671",
            phoneAuthenticated = true
        )

        val host = webTestClient.post()
            .uri("/users/")
            .bodyValue(hostSignUpRequest)
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody(UserController.SignUpResponse::class.java)
            .returnResult()
            .responseBody!!

        // 게스트 생성
        val guestSignUpRequest = UserController.SignUpRequest(
            email = "guest@email.com",
            nickname = "guest",
            age = 25,
            gender = Gender.F,
            introduction = "guest intro",
            password = "password",
            phoneNumber = "01012345672",
            phoneAuthenticated = true
        )

        val guest = webTestClient.post()
            .uri("/users/")
            .bodyValue(guestSignUpRequest)
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody(UserController.SignUpResponse::class.java)
            .returnResult()
            .responseBody!!

        // Gathering 생성
        val anyString = "string"
        val fee = 0
        val openGatheringRequest = OpenGatheringRequest(
            hostUuid = host.uuid,
            approveType = GatheringApproveType.FIRST_IN,
            minCapacity = 1,
            maxCapacity = 10,
            genderRatioEnabled = false,
            minAge = 20,
            maxAge = 40,
            fee = fee,
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

        val gathering = webTestClient.post()
            .uri("/gatherings")
            .bodyValue(openGatheringRequest)
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody(OpenGatheringResponse::class.java)
            .returnResult()
            .responseBody!!

        // when
        val joinRequest = JoinGatheringRequest(userUuid = guest.uuid)

        webTestClient.post()
            .uri("/gatherings/${gathering.uuid}/join")
            .bodyValue(joinRequest)
            .exchange()
            .expectStatus().is2xxSuccessful

        // then
        val guestCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM guests WHERE gathering_uuid = ?",
            Long::class.java,
            gathering.uuid
        )

        assertThat(guestCount).isEqualTo(1L)
    }

    @Test
    fun `유료 모임 참여`() {
        // given
        // 호스트 생성
        val hostSignUpRequest = UserController.SignUpRequest(
            email = "host@email.com",
            nickname = "host",
            age = 30,
            gender = Gender.M,
            introduction = "host intro",
            password = "password",
            phoneNumber = "01012345671",
            phoneAuthenticated = true
        )

        val host = webTestClient.post()
            .uri("/users/")
            .bodyValue(hostSignUpRequest)
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody(UserController.SignUpResponse::class.java)
            .returnResult()
            .responseBody!!

        // 게스트 생성
        val guestSignUpRequest = UserController.SignUpRequest(
            email = "guest@email.com",
            nickname = "guest",
            age = 25,
            gender = Gender.F,
            introduction = "guest intro",
            password = "password",
            phoneNumber = "01012345672",
            phoneAuthenticated = true
        )

        val guest = webTestClient.post()
            .uri("/users/")
            .bodyValue(guestSignUpRequest)
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody(UserController.SignUpResponse::class.java)
            .returnResult()
            .responseBody!!

        // 유료 Gathering 열기
        val anyString = "string"
        val fee = 10000
        val openGatheringRequest = OpenGatheringRequest(
            hostUuid = host.uuid,
            approveType = GatheringApproveType.FIRST_IN,
            minCapacity = 1,
            maxCapacity = 10,
            genderRatioEnabled = false,
            minAge = 20,
            maxAge = 40,
            fee = fee,
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

        val gathering = webTestClient.post()
            .uri("/gatherings")
            .bodyValue(openGatheringRequest)
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody(OpenGatheringResponse::class.java)
            .returnResult()
            .responseBody!!

        // 결제 준비
        val paymentId = "test-payment-${System.currentTimeMillis()}"
        val preparePaymentRequest = PreparePaymentRequest(
            productType = ProductType.GATHERING,
            productUuid = gathering.uuid,
            amount = fee,
            productName = "테스트 모임",
            buyerUuid = guest.uuid,
            buyerEmail = "guest@email.com",
            buyerName = "guest",
            buyerPhone = "01012345672"
        )

        webTestClient.post()
            .uri("/payments/{paymentId}/prepare", paymentId)
            .bodyValue(preparePaymentRequest)
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody(PreparePaymentResponse::class.java)

        // when
        val joinRequest = JoinGatheringRequest(
            userUuid = guest.uuid,
            confirmPaymentRequest = ConfirmPaymentRequest(
                paymentId = paymentId,
                paymentToken = "test-token",
                txId = "test-tx-id",
                amount = fee
            )
        )

        webTestClient.post()
            .uri("/gatherings/${gathering.uuid}/join")
            .bodyValue(joinRequest)
            .exchange()
            .expectStatus().is2xxSuccessful

        // then
        val guestCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM guests WHERE gathering_uuid = ?",
            Long::class.java,
            gathering.uuid
        )
        assertThat(guestCount).isEqualTo(1L)

        val paymentCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM payments WHERE payment_id = ?",
            Long::class.java,
            paymentId
        )
        assertThat(paymentCount).isEqualTo(1L)

        val paymentStatus = jdbcTemplate.queryForObject(
            "SELECT status FROM payments WHERE payment_id = ?",
            String::class.java,
            paymentId
        )
        assertThat(paymentStatus).isEqualTo("PAID")
    }
}