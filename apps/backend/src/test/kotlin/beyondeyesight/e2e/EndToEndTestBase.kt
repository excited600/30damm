package beyondeyesight.e2e

import beyondeyesight.TestConfig
import beyondeyesight.model.SignupRequest
import beyondeyesight.model.SignupResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.reactive.server.WebTestClient
import tools.jackson.databind.ObjectMapper

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestConfig::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class EndToEndTestBase {

    @LocalServerPort
    private var port: Int = 0

    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    @Qualifier("testObjectMapper")
    lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        webTestClient = WebTestClient
            .bindToServer()
            .baseUrl("http://localhost:$port")
            .build()
    }

    @BeforeAll
    fun initialClean() {
        cleanDatabase()
    }

    @AfterEach
    fun cleanUp() {
        cleanDatabase()
    }

    protected fun signUp(email: String, nickname: String, password: String): SignupResponse {
        return webTestClient.post()
            .uri("/api/v1/users/signup")
            .bodyValue(SignupRequest(email = email, password = password, nickname = nickname))
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody(SignupResponse::class.java)
            .returnResult()
            .responseBody!!
    }

    private fun cleanDatabase() {
        val tables = jdbcTemplate.queryForList(
            """
                SELECT tablename FROM pg_tables 
                WHERE schemaname = 'public' 
                AND tablename != 'flyway_schema_history'
                """,
            String::class.java
        )

        if (tables.isNotEmpty()) {
            jdbcTemplate.execute(
                "TRUNCATE TABLE ${tables.joinToString(", ")} RESTART IDENTITY CASCADE"
            )
        }
    }
}