package beyondeyesight.integration

import beyondeyesight.TestConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
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

    @AfterEach
    fun cleanUp() {
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