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
        // TODO 이렇게 하면 모든 외부 요청에 포트원 설정이 들어갈거같은데... 다른 서드파티(카카오메시지 등) 이용할 때 문제될듯
        return WebClient.builder()
            .baseUrl(properties.baseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "PortOne ${properties.apiSecret}")
            .build()
    }
}

