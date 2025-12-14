package beyondeyesight.domain.exception

import java.util.UUID

class ResourceNotFoundException private constructor(message: String): ClientException(
    statusCode = 404,
    message = message
) {
    companion object {
        fun byUuid(resourceName: String, resourceUuid: UUID): ResourceNotFoundException {
            return ResourceNotFoundException("$resourceName with ID $resourceUuid not found.")
        }

        fun byField(resourceName: String, fieldName: String, fieldValue: Any): ResourceNotFoundException {
            return ResourceNotFoundException("$resourceName with $fieldName = $fieldValue not found.")
        }
    }
}