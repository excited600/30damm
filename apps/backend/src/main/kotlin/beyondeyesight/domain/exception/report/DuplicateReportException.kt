package beyondeyesight.domain.exception.report

import beyondeyesight.domain.exception.ClientException

class DuplicateReportException(
    targetType: String,
    targetUuid: String,
) : ClientException(
    statusCode = 409,
    message = "이미 해당 $targetType($targetUuid)에 대한 신고를 접수하셨습니다."
)
