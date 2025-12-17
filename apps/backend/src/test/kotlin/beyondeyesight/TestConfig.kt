package beyondeyesight

import beyondeyesight.domain.model.payment.*
import beyondeyesight.domain.service.LockService
import beyondeyesight.domain.service.payment.PaymentGateway
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@TestConfiguration
class TestConfig {

    @Bean("testObjectMapper")
    fun testObjectMapper(): ObjectMapper {
        return JsonMapper.builder()
            .addModule(KotlinModule.Builder().build())
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .build()
    }

    @Bean
    @Primary
    fun fakePaymentGateway(): PaymentGateway = FakePaymentGateway()

    @Bean
    @Primary
    fun fakeLockService(): LockService = FakeLockService()
}

class FakePaymentGateway : PaymentGateway {
    private val payments = ConcurrentHashMap<String, FakePaymentData>()

    data class FakePaymentData(
        val paymentId: String,
        val amount: Int,
        var status: Status = Status.READY
    )

    override fun preRegisterPayment(paymentId: String, totalAmount: Int, currency: Currency) {
        payments[paymentId] = FakePaymentData(
            paymentId = paymentId,
            amount = totalAmount,
            status = Status.READY
        )
    }

    override fun getPayment(paymentId: String): Payment {
        val data = payments[paymentId]
            ?: return PaymentReady(
                id = paymentId,
                transactionId = "tx-$paymentId",
                merchantId = "merchant-test",
                amount = PaymentAmount(total = 0, paid = 0, cancelled = 0, taxFree = 0)
            )

        return when (data.status) {
            Status.READY -> PaymentReady(
                id = paymentId,
                transactionId = "tx-$paymentId",
                merchantId = "merchant-test",
                amount = PaymentAmount(total = data.amount, paid = 0, cancelled = 0, taxFree = 0)
            )
            Status.PAID -> PaymentPaid(
                id = paymentId,
                transactionId = "tx-$paymentId",
                merchantId = "merchant-test",
                amount = PaymentAmount(total = data.amount, paid = data.amount, cancelled = 0, taxFree = 0),
                paidAt = LocalDateTime.now()
            )
            Status.CANCELLED -> PaymentCancelled(
                status = Status.CANCELLED,
                id = paymentId,
                transactionId = "tx-$paymentId",
                merchantId = "merchant-test",
                storeId = "store-test",
                amount = PaymentAmount(total = data.amount, paid = 0, cancelled = data.amount, taxFree = 0),
                cancelledAt = LocalDateTime.now(),
                cancellations = null
            )
            else -> PaymentReady(
                id = paymentId,
                transactionId = "tx-$paymentId",
                merchantId = "merchant-test",
                amount = PaymentAmount(total = data.amount, paid = 0, cancelled = 0, taxFree = 0)
            )
        }
    }

    override fun confirm(paymentId: String, paymentToken: String, txId: String, amount: Int) {
        val data = payments[paymentId] ?: throw IllegalStateException("Payment not found: $paymentId")
        data.status = Status.PAID
    }

    override fun cancelPayment(paymentId: String, reason: String, amount: Int?): CancelPaymentResponse {
        val data = payments[paymentId] ?: throw IllegalStateException("Payment not found: $paymentId")
        data.status = Status.CANCELLED
        return CancelPaymentResponse(
            cancellation = PaymentCancellation(
                status = PaymentCancellationStatus.SUCCEEDED,
                id = "cancel-$paymentId",
                totalAmount = amount ?: data.amount,
                reason = reason,
                cancelledAt = LocalDateTime.now()
            )
        )
    }

    override fun getPaymentClientConfig(): PaymentClientConfig {
        return PaymentClientConfig(
            storeId = "test-store-id",
            channelKey = "test-channel-key"
        )
    }
}

class FakeLockService : LockService {
    private val locks = ConcurrentHashMap<String, String>()

    override fun tryLock(resourceName: String, resourceId: String, expire: Duration): String? {
        val key = "$resourceName:$resourceId"
        val token = UUID.randomUUID().toString()
        return if (locks.putIfAbsent(key, token) == null) token else null
    }

    override fun lockWithRetry(
        resourceName: String,
        resourceId: String,
        expire: Duration,
        waitTimeout: Duration,
        retryInterval: Duration
    ): String? {
        val key = "$resourceName:$resourceId"
        val token = UUID.randomUUID().toString()
        val deadline = System.currentTimeMillis() + waitTimeout.toMillis()

        while (System.currentTimeMillis() < deadline) {
            if (locks.putIfAbsent(key, token) == null) {
                return token
            }
            Thread.sleep(retryInterval.toMillis().coerceAtMost(100))
        }
        return null
    }

    override fun unlock(resourceName: String, resourceId: String, token: String) {
        val key = "$resourceName:$resourceId"
        locks.remove(key, token)
    }
}