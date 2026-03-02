package beyondeyesight.config

import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

fun currentUserUuid(): UUID {
    val principal = SecurityContextHolder.getContext().authentication?.principal as? String
        ?: throw IllegalStateException("No authenticated user found")
    return UUID.fromString(principal)
}
