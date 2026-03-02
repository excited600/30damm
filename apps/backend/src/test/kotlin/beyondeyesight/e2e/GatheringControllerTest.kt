package beyondeyesight.e2e

import beyondeyesight.model.ConfirmPaymentRequest
import beyondeyesight.model.GatheringApproveType
import beyondeyesight.model.GatheringCategory
import beyondeyesight.model.GatheringDayOfWeek
import beyondeyesight.model.GatheringScheduleType
import beyondeyesight.model.GatheringSubCategory
import beyondeyesight.model.GatheringWeeklyScheduleSummary
import beyondeyesight.model.JoinGatheringRequest
import beyondeyesight.model.OpenGatheringRequest
import beyondeyesight.model.OpenGatheringResponse
import beyondeyesight.model.PreparePaymentRequest
import beyondeyesight.model.PreparePaymentResponse
import beyondeyesight.model.ProductType
import beyondeyesight.model.WeeklyScheduleSeriesRequest
import org.assertj.core.api.Assertions.assertThat
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import kotlin.test.Test

class GatheringControllerTest : EndToEndTestBase() {

    @Test
    fun `Schedule Series`() {
        val host = signUp(email = "host@email.com", nickname = "hostname", password = "password1234")

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
            hostUuid = host.userUuid,
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
            .uri("/api/v1/series")
            .header("Authorization", "Bearer ${host.accessToken}")
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
        val host = signUp(email = "host@email.com", nickname = "hostname", password = "password1234")

        val anyString = "string"
        val request = OpenGatheringRequest(
            title = anyString,
            description = anyString,
            category = GatheringCategory.ACTIVITY,
            minCapacity = 2,
            maxCapacity = 10,
            isGenderRatioEnabled = false,
            isFree = false,
            isSplit = false,
            location = anyString,
            date = LocalDate.now().plusDays(7),
            startTime = "19:00",
            duration = 120,
            price = 1000,
        )

        webTestClient.post()
            .uri("/api/v1/gatherings")
            .header("Authorization", "Bearer ${host.accessToken}")
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
        // given
        val host = signUp(email = "host@email.com", nickname = "hostname", password = "password1234")
        val guest = signUp(email = "guest@email.com", nickname = "guestnick", password = "password1234")

        val anyString = "string"
        val openGatheringRequest = OpenGatheringRequest(
            title = anyString,
            description = anyString,
            category = GatheringCategory.ACTIVITY,
            minCapacity = 2,
            maxCapacity = 10,
            isGenderRatioEnabled = false,
            isFree = true,
            isSplit = false,
            location = anyString,
            date = LocalDate.now().plusDays(7),
            startTime = "19:00",
            duration = 120,
        )

        val gathering = webTestClient.post()
            .uri("/api/v1/gatherings")
            .header("Authorization", "Bearer ${host.accessToken}")
            .bodyValue(openGatheringRequest)
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody(OpenGatheringResponse::class.java)
            .returnResult()
            .responseBody!!

        // when
        val joinRequest = JoinGatheringRequest(userUuid = guest.userUuid)

        webTestClient.post()
            .uri("/api/v1/gatherings/${gathering.gatheringUuid}/join")
            .header("Authorization", "Bearer ${guest.accessToken}")
            .bodyValue(joinRequest)
            .exchange()
            .expectStatus().is2xxSuccessful

        // then
        val guestCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM guests WHERE gathering_uuid = ?",
            Long::class.java,
            gathering.gatheringUuid
        )

        assertThat(guestCount).isEqualTo(1L)
    }

    @Test
    fun `유료 모임 참여`() {
        // given
        val host = signUp(email = "host@email.com", nickname = "hostname", password = "password1234")
        val guest = signUp(email = "guest@email.com", nickname = "guestnick", password = "password1234")

        val anyString = "string"
        val fee = 10000
        val openGatheringRequest = OpenGatheringRequest(
            title = anyString,
            description = anyString,
            category = GatheringCategory.ACTIVITY,
            minCapacity = 2,
            maxCapacity = 10,
            isGenderRatioEnabled = false,
            isFree = false,
            isSplit = false,
            location = anyString,
            date = LocalDate.now().plusDays(7),
            startTime = "19:00",
            duration = 120,
            price = fee,
        )

        val gathering = webTestClient.post()
            .uri("/api/v1/gatherings")
            .header("Authorization", "Bearer ${host.accessToken}")
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
            productUuid = gathering.gatheringUuid,
            amount = fee,
            productName = "테스트 모임",
            buyerUuid = guest.userUuid,
            buyerEmail = "guest@email.com",
            buyerName = "guestnick",
            buyerPhone = "01012345672"
        )

        webTestClient.post()
            .uri("/api/v1/payments/{paymentId}/prepare", paymentId)
            .header("Authorization", "Bearer ${guest.accessToken}")
            .bodyValue(preparePaymentRequest)
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody(PreparePaymentResponse::class.java)

        // when
        val joinRequest = JoinGatheringRequest(
            userUuid = guest.userUuid,
            confirmPaymentRequest = ConfirmPaymentRequest(
                paymentId = paymentId,
                paymentToken = "test-token",
                txId = "test-tx-id",
                amount = fee
            )
        )

        webTestClient.post()
            .uri("/api/v1/gatherings/${gathering.gatheringUuid}/join")
            .header("Authorization", "Bearer ${guest.accessToken}")
            .bodyValue(joinRequest)
            .exchange()
            .expectStatus().is2xxSuccessful

        // then
        val guestCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM guests WHERE gathering_uuid = ?",
            Long::class.java,
            gathering.gatheringUuid
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
