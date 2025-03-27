package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.validation.PointValidator;

import java.util.List;

public class PointService {
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public PointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

    /**
     * 특정 유저의 포인트를 조회
     */
    public UserPoint getPoint(long userId) {
        // 리팩토링 이 예외는 [ 형식 : 절대적 ]이라서 UserPoint에 위치
        // if (userId <= 0) {
        //     throw new IllegalArgumentException("조회시 userId는 양수여야 합니다.");
        // }
        return userPointTable.selectById(userId);
    }

    /**
     * 특정 유저의 포인트를 충전하는 기능
     */
    public UserPoint charge(long userId, long amount) {

        // 요청 파라미터의 형식/논리 검증 : 충전 요청 값 - UserPoint(point) 별개이고 가변적이지도 않다
        if (amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }

        // 기존 포인트 조회
        UserPoint current = userPointTable.selectById(userId);

        // [ 정책 검증 ] : 가변적 규칙은 Validator로 위임
        PointValidator.validateCharge(userId, amount, current.point());

        // 계산은 서비스에서 하는게 맞다
        long total = current.point() + amount;

        // 포인트 저장
        UserPoint updated = userPointTable.insertOrUpdate(userId, total);

        // 충전 내역 저장 : 인메모리 저장
        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, updated.updateMillis());

        return updated; // 저장된 값을 그대로 반환
    }

    /**
     * 특정 유저의 포인트를 사용하는 기능
     */
    public UserPoint use(long userId, long amount) {

        // [ 형식/논리 ]
        if (amount <= 0) {
            throw new IllegalArgumentException("사용 금액은 0보다 커야 합니다.");
        }

        // 기존 포인트 조회
        UserPoint current = userPointTable.selectById(userId);

        // [ 정책 검증 ]
        PointValidator.validateUse(amount, current.point());

        // 포인트 차감 계산
        long remaining = current.point() - amount;

        // 포인트 저장
        UserPoint updated = userPointTable.insertOrUpdate(userId, remaining);

        // 사용 내역 저장
        pointHistoryTable.insert(userId, amount, TransactionType.USE, updated.updateMillis());

        return updated;
    }

    /**
     * 특정 유저의 포인트 사용/충전 내역을 조회하는 기능
     */
    public List<PointHistory> getHistories(long userId) {

        // [형식/논리 검증] UserPoint 객체를 만들지 않기 때문에
        // userId의 유효성(양수 여부)을 여기서 직접 검사한다
        if (userId <= 0) {
            throw new IllegalArgumentException("userId는 양수여야 합니다.");
        }

        return pointHistoryTable.selectAllByUserId(userId);
    }
}