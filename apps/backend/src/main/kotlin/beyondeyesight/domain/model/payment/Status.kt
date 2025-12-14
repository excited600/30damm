package beyondeyesight.domain.model.payment

enum class Status {
    READY,
    PAID,              // 결제 완료
    PAY_PENDING,           // 결제 대기
    VIRTUAL_ACCOUNT_ISSUED,
    FAILED,            // 결제 실패
    CANCELLED,         // 전액 취소
    PARTIAL_CANCELLED  // 부분 취소
}