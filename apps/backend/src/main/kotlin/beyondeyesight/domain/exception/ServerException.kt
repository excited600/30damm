package beyondeyesight.domain.exception

open class ServerException(
    val statusCode: Int,
    override val message: String,
): RuntimeException(message)