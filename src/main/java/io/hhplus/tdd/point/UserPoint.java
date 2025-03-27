package io.hhplus.tdd.point;

// 사용자 포인트 모델
public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    // [ 절대적 ] 유효하지 않은 ID로 객체 생성을 방지
    // 모든 필드(id, point, updateMillis)를 포함한 canonical constructor (항상 new UserPoint(...) 시 자동 실행)
    // 포인트 충전이든 포인트 사용이든 UserPoint 객체를 생성할 때마다 반드시 통과해야 하는 절대 조건
    public UserPoint {
        if (id <= 0) {
            throw new IllegalArgumentException("userId는 양수여야 합니다.");
        }

    }

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }
}
