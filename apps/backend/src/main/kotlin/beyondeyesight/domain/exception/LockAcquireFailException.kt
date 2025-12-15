package beyondeyesight.domain.exception

import java.time.Duration
import java.util.UUID

class LockAcquireFailException private constructor(message: String): ServerException(statusCode = 500, message = message) {

    companion object {
        fun forResource(resourceName: String, resourceId: String, duration: Duration): LockAcquireFailException {
            return LockAcquireFailException("Failed to acquire lock for resource: $resourceName with id: $resourceId for ${duration.toSeconds()} seconds")
        }

        fun forResource(resourceName: String, resourceUuid: UUID, duration: Duration): LockAcquireFailException {
            return LockAcquireFailException("Failed to acquire lock for resource: $resourceName with uuid: $resourceUuid for ${duration.toSeconds()} seconds")
        }
    }

}