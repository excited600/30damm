package beyondeyesight.domain.exception

import java.util.UUID

class DataIntegrityException(
    message: String
) : ServerException(
    statusCode = 500,
    message = message
) {
    constructor(tableName: String, cause: String) : this("$tableName has data integrity issue with reason: $cause")

    constructor(tableName: String, resourceUuid: UUID, cause: String) : this("$tableName with id $resourceUuid has data integrity issue with reason: $cause")
}