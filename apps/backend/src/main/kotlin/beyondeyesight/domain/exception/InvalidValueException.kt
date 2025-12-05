package beyondeyesight.domain.exception

class InvalidValueException(
    valueName: String,
    value: Any,
    reason: String? = null
) : ClientException(
    statusCode = 400,
    message = "$valueName has invalid value: $value" +
            (reason?.let { " because $it" } ?: "")
)