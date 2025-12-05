package beyondeyesight.domain.exception

import java.util.UUID

class ResourceNotFoundException(resourceName: String, resourceId: UUID): ClientException(
    statusCode = 404,
    message = "$resourceName with ID $resourceId not found."
)