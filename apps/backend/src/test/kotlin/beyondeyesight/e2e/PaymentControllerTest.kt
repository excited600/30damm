package beyondeyesight.e2e

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.test.Test

class PaymentControllerTest : EndToEndTestBase() {

    @Autowired
    lateinit var webClient: WebClient

    @Test
    fun `결제`() {
        val paymentId = generatePaymentId()

        // 1. 빌링키 발급
        val billingKeyResponse = webClient.post()
            .uri("/billing-keys")
            .bodyValue(
                mapOf(
                    "channelKey" to "channel-key-613a0be6-f1b6-4d69-83dc-ca7635301d1c",
                    "method" to mapOf(
                        "card" to mapOf(
                            "credential" to mapOf(
                                "number" to "5376990029908545",
                                "expiryYear" to "30",
                                "expiryMonth" to "03",
                                "birthOrBusinessRegistrationNumber" to "911211",
                                "passwordTwoDigits" to "45"
                            )
                        )
                    )
                )
            )
            .exchangeToMono { clientResponse ->
                if (clientResponse.statusCode().isError) {
                    clientResponse.bodyToMono(String::class.java)
                        .map { body ->
                            throw RuntimeException("Error: ${clientResponse.statusCode()} - $body")
                        }
                } else {
                    clientResponse.bodyToMono(BillingKeyResponse::class.java)
                }
            }
            .block()

        val billingKey = billingKeyResponse?.billingKeyInfo?.billingKey

// 2. 빌링키로 결제
        val paymentResponse = webClient.post()
            .uri("/payments/$paymentId/billing-key")
            .bodyValue(
                mapOf(
                    "billingKey" to billingKey,
                    "orderName" to "테스트 상품",
                    "amount" to mapOf(
                        "total" to 500
                    ),
                    "currency" to "KRW"
                )
            )
            .retrieve()
            .bodyToMono(PaymentResponse::class.java)
            .block()

// 3. 결제 검증
        val payment = webClient.get()
            .uri("/payments/$paymentId")
            .retrieve()
            .bodyToMono(Payment::class.java)
            .block()

        assert(payment?.status == "PAID")

    }

    // Response DTOs
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class BillingKeyResponse(
        val billingKeyInfo: BillingKeyInfoSummary,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class BillingKeyInfoSummary(
        val billingKey: String,
        val issuedAt: String
    )

    data class PaymentResponse(
        val payment: BillingKeyPaymentSummary
    )

    data class BillingKeyPaymentSummary(
        val pgTxId: String,
        val paidAt: String
    )


    @JsonIgnoreProperties(ignoreUnknown = true)
    data class PaymentAmount(
        val total: Int

    )
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Payment(
        val status: String,
        val id: String,
        val transactionId: String,
        val amount: PaymentAmount,
        val currency: String
    )

    fun generatePaymentId(): String {
        val timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
        )
        val random = UUID.randomUUID().toString().take(4).uppercase()
        return "[TEST] PAY-$timestamp-$random"
    }
}