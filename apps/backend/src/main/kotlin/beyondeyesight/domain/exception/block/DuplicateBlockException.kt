package beyondeyesight.domain.exception.block

import beyondeyesight.domain.exception.ClientException

class DuplicateBlockException(
    blockedUuid: String,
) : ClientException(
    statusCode = 409,
    message = "이미 해당 사용자($blockedUuid)를 차단하셨습니다."
)
