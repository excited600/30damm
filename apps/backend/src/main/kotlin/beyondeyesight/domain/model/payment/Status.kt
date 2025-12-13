package beyondeyesight.domain.model.payment

enum class Status {
    PENDING,           // 결제 대기
    READY,
    VIRTUAL_ACCOUNT_ISSUED,
    PAID,              // 결제 완료
    FAILED,            // 결제 실패
    CANCELLED,         // 전액 취소
    PARTIAL_CANCELLED  // 부분 취소
}