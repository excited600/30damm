package beyondeyesight.integration

import beyondeyesight.domain.model.UserEntity
import beyondeyesight.domain.repository.UserRepository
import beyondeyesight.model.OpenGatheringRequest
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import kotlin.test.Test

class GatheringControllerTest: EndToEndTestBase() {

    @Autowired
    lateinit var userRepository: UserRepository

    @Test
    fun `Gathering Open`() {
        val host = userRepository.save(
            UserEntity.signUp(
                email = "email",
                nickname = "nickname",
                age = 25,
                gender = UserEntity.Gender.M,
                introduction = "intro",
                password = "password",
                phoneNumber = "01012345671",
                phoneAuthenticated = true,
            )
        )
        val anyInt = 1
        val anyString = "string"
        val minFee = 1_000
        val request = OpenGatheringRequest(
            hostUuid = host.uuid,
            approveType = OpenGatheringRequest.ApproveType.FIRST_IN,
            minCapacity = anyInt,
            maxCapacity = anyInt + 1,
            genderRatioEnabled = false,
            minAge = anyInt,
            maxAge = anyInt + 1,
            fee = minFee,
            discountEnabled = false,
            offline = true,
            place = anyString,
            category = OpenGatheringRequest.Category.ACTIVITY,
            subCategory = OpenGatheringRequest.SubCategory.HOME_PARTY,
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
            .expectBody(beyondeyesight.model.OpenGatheringResponse::class.java)

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