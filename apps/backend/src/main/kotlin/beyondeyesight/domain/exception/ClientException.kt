package beyondeyesight.domain.exception

open class ClientException(
    val statusCode: Int,
    override val message: String,
) : RuntimeException(message)