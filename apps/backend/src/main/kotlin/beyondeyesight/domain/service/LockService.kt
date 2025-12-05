package beyondeyesight.domain.service

import java.time.Duration

interface LockService {
    fun tryLock(
        resourceName: String,
        resourceId: String,
        expire: Duration = Duration.ofSeconds(10)
    ): String?

    fun lockWithRetry(
        resourceName: String,
        resourceId: String,
        expire: Duration = Duration.ofSeconds(10),
        waitTimeout: Duration = Duration.ofSeconds(5),
        retryInterval: Duration = Duration.ofMillis(100)
    ) : String?

    fun unlock(resourceName: String, resourceId: String, token: String)
}