package io.hhplus.tdd.point;

// 포인트 사용 내역 모델
public record PointHistory(
        long id,
        long userId,
        long amount,
        TransactionType type,
        long updateMillis
) {
}
