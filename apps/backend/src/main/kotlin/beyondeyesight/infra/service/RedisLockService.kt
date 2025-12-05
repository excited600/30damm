package beyondeyesight.infra.service

import beyondeyesight.domain.service.LockService
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.UUID

@Service
class RedisLockService(
    private val redisTemplate: StringRedisTemplate
): LockService {
    private val unlockScript = DefaultRedisScript<Long>().apply {
        setScriptText("""
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1])
            else
                return 0
            end
        """.trimIndent())
        this.resultType = Long::class.java
    }

    override fun tryLock(
        resourceName: String,
        resourceId: String,
        expire: Duration,
    ): String? {
        val token = UUID.randomUUID().toString()
        val success = redisTemplate.opsForValue()
            .setIfAbsent("lock:$resourceName:$resourceId", token, expire)

        return if (success == true) token else null
    }

    override fun lockWithRetry(
        resourceName: String,
        resourceId: String,
        expire: Duration,
        waitTimeout: Duration,
        retryInterval: Duration
    ): String? {
        val deadline = System.currentTimeMillis() + waitTimeout.toMillis()

        while (System.currentTimeMillis() < deadline) {
            val token = tryLock(resourceName, resourceId, expire)
            if (token != null) return token

            Thread.sleep(retryInterval.toMillis())
        }
        return null
    }

    override fun unlock(resourceName: String, resourceId: String, token: String) {
        redisTemplate.execute(
            unlockScript,
            listOf("lock:$resourceName:$resourceId"),
            token
        )
    }
}