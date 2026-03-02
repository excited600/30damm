package beyondeyesight.e2e

import beyondeyesight.model.LoginRequest
import beyondeyesight.model.LoginResponse
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

class UserControllerTest : EndToEndTestBase() {

    @Test
    fun `회원가입`() {
        // when
        val response = signUp(email = "user@email.com", nickname = "usernick", password = "password1234")

        // then
        assertThat(response.userUuid).isNotNull()
        assertThat(response.accessToken).isNotBlank()
        assertThat(response.refreshToken).isNotBlank()

        val userCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users WHERE email = ?",
            Long::class.java,
            "user@email.com"
        )
        assertThat(userCount).isEqualTo(1L)
    }

    @Test
    fun `로그인`() {
        // given
        signUp(email = "user@email.com", nickname = "usernick", password = "password1234")

        // when
        val loginResponse = webTestClient.post()
            .uri("/api/v1/users/login")
            .bodyValue(LoginRequest(email = "user@email.com", password = "password1234"))
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody(LoginResponse::class.java)
            .returnResult()
            .responseBody!!

        // then
        assertThat(loginResponse.userUuid).isNotNull()
        assertThat(loginResponse.accessToken).isNotBlank()
        assertThat(loginResponse.refreshToken).isNotBlank()
    }
}
