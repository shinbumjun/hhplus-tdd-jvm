package io.hhplus.tdd.validation;

// [ 정책 : 가변적 ] validation으로 위임 -> 정책만 담당
public class PointValidator {
    // 포인트 충전
    public static void validateCharge(long userId, long amount, long currentPoint) {

        if (amount > 100_000) {
            throw new IllegalArgumentException("충전 금액은 100,000원 초과할 수 없습니다.");
        }

        if (currentPoint + amount > 100_000) {
            throw new IllegalArgumentException("최대 보유 포인트는 100,000원을 초과할 수 없습니다.");
        }
    }

    public static void validateUse(long amount, long currentPoint) {
        if (amount > currentPoint) {
            throw new IllegalArgumentException("보유 포인트보다 많은 금액은 사용할 수 없습니다.");
        }
    }
}

