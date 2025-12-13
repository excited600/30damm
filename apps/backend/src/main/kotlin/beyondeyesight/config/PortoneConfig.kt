package beyondeyesight.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

@ConfigurationProperties(prefix = "portone")
class PortoneProperties(
    val apiSecret: String,
    val storeId: String,
    val channelKey: String,
    val baseUrl: String,
)

@Configuration
@EnableConfigurationProperties(PortoneProperties::class)
class PortoneConfig(
    private val properties: PortoneProperties
) {

    @Bean
    fun portOneWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl(properties.baseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "PortOne ${properties.apiSecret}")
            .build()
    }
}

