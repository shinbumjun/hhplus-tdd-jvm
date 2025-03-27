package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class PointServiceIntegrationTest {

    @Autowired
    private PointService pointService; // 서비스 객체를 자동으로 주입

    @Autowired
    private UserPointTable userPointTable;

    @Autowired
    private PointHistoryTable pointHistoryTable;

    @Test
    @DisplayName("조회시 : 유저 ID로 조회하면 유저 포인트 정보를 반환한다")
    void getPointTest1() {
        // given : 테스트 준비 (가짜 데이터와 동작 정의)
        long userId = 1L;
        UserPoint expected = new UserPoint(userId, 2000, System.currentTimeMillis());

        // 이 함수 호출되면 무조건 expected 줘! (Stub)
        when(userPointTable.selectById(userId))
                .thenReturn(expected);

        // When: 실제 호출
        UserPoint result = pointService.getPoint(userId); // 내부적으로 userPointTable.selectById(userId) 호출

        // Then: 기대한 값과 실제 결과를 비교 (결과 검증)
        assertEquals(expected, result);

    }

    @Test
    @DisplayName("조회시 : 유저 ID가 0 이하이면 에러 메시지를 던진다")
    void getPointTest2() {
        // given
        long userId = 0L;

        // when : 예외 발생 테스트 패턴 : 이 코드를 실행하면 IllegalArgumentException이 반드시 터져야 해
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.getPoint(userId);
        });

        // then
        assertEquals("userId는 양수여야 합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("충전시 : 기존 포인트가 없으면 그대로 저장된다")
    void chargeTest1() {
        // given
        long userId = 1L;
        long charge = 30_000L;

        when(userPointTable.selectById(userId))
                .thenReturn(new UserPoint(userId, 0L, System.currentTimeMillis()));

        // when
        UserPoint result = pointService.charge(userId, charge);

        // then
        assertEquals(charge, result.point());
    }

    @Test
    @DisplayName("충전시 : 충전 금액이 0 이하이면 에러 메시지를 던진다")
    void chargeTest2() {
        long userId = 1L;
        long charge = 0L;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.charge(userId, charge);
        });

        assertEquals("충전 금액은 0보다 커야 합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("충전시 : 충전 금액이 100,000 초과하면 에러 메시지를 던진다")
    void chargeTest3() {
        // given
        long userId = 1L;
        long charge = 100_001L;

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.charge(userId, charge);
        });

        // then
        assertEquals("충전 금액은 100,000원 초과할 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("충전시 : 기존 포인트와 충전 금액의 합이 100,000 초과하면 에러 메시지를 던진다")
    void chargeTest4() {
        // given
        long userId = 1L;
        long balance = 90_000L; // 기존에 보유한 포인트
        long charge = 20_000L; // 충전하려는 포인트

        // (Stub)가짜 데이터 설정: selectById(1L)을 호출하면 → 포인트 90,000짜리 유저 객체를 줘!
        when(userPointTable.selectById(userId))
                .thenReturn(new UserPoint(userId, balance, System.currentTimeMillis()));

        // when
        // 내부에서 selectById를 요청하지 않았는데 왜 가짜값(90000)이 들어갔지?
        // charge 메서드를 호출하면 그 안에서 기존 포인트를 조회하기 때문에, charge만 호출해도 Stub(가짜 데이터)이 동작한다
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.charge(userId, charge);
        });

        // then
        assertEquals("최대 보유 포인트는 100,000원을 초과할 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("충전시 : 기존 포인트에 충전 금액이 누적되어 저장된다")
    void chargeTest6() {
        // given
        long userId = 1L;
        long existingPoint = 50_000L; // 기존 보유 포인트
        long chargeAmount = 30_000L; // 충전할 포인트
        long expectedTotal = existingPoint + chargeAmount;

        // (Stub)기존 포인트 조회 시 → 기존값 반환하도록 설정
        when(userPointTable.selectById(userId))
                .thenReturn(new UserPoint(userId, existingPoint, System.currentTimeMillis()));

        // (Stub)저장 시 → 누적된 포인트가 저장된 객체를 반환하도록 설정
        // when(userPointTable.insertOrUpdate(userId, expectedTotal))
        //        .thenReturn(new UserPoint(userId, expectedTotal, System.currentTimeMillis()));

        // when: 충전 기능 실행
        UserPoint result = pointService.charge(userId, chargeAmount);

        // then: 결과 포인트가 누적된 값과 일치하는지 확인
        assertEquals(expectedTotal, result.point()); // 객체 전체를 비교하지 않고 값만 비교
    }

    @Test
    @DisplayName("사용시 : userId가 0 이하이면 에러 메시지를 던진다")
    void chargeTest7() {
        // given
        long userId = 0L;
        long amount = 10_000L;

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.use(userId, amount);
        });

        // then
        assertEquals("userId는 양수여야 합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("사용시 : 사용 금액이 0 이하이면 에러 메시지를 던진다")
    void chargeTest8() {
        // given
        long userId = 1L;
        long amount = 0L;

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.use(userId, amount);
        });

        // then
        assertEquals("사용 금액은 0보다 커야 합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("사용시 : 보유 포인트보다 많은 금액을 사용하면 에러 메시지를 던진다")
    void chargeTest9() {
        // given
        long userId = 1L;
        long currentBalance = 5_000L; // 현재 보유 포인트
        long usageAmount = 10_000L;   // 사용하려는 포인트

        // 기존 보유 포인트 설정 (Stub)
        when(userPointTable.selectById(userId))
                .thenReturn(new UserPoint(userId, currentBalance, System.currentTimeMillis()));

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.use(userId, usageAmount);
        });

        // then
        assertEquals("보유 포인트보다 많은 금액은 사용할 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("사용시 : 정상적으로 포인트가 차감되어 저장된다")
    void chargeTest10() {
        // given
        long userId = 1L;
        long currentBalance = 20_000L;  // 현재 보유 포인트
        long usageAmount = 5_000L;      // 사용할 포인트
        long expectedRemaining = currentBalance - usageAmount; // 남은 포인트

        // (Stub) 현재 보유 포인트 조회
        when(userPointTable.selectById(userId))
                .thenReturn(new UserPoint(userId, currentBalance, System.currentTimeMillis()));

        // (Stub) 차감 후 저장되는 포인트
        // when(userPointTable.insertOrUpdate(userId, expectedRemaining))
        //         .thenReturn(new UserPoint(userId, expectedRemaining, System.currentTimeMillis()));

        // when
        UserPoint result = pointService.use(userId, usageAmount);

        // then
        assertEquals(expectedRemaining, result.point());  // 포인트가 제대로 차감되었는지 검증
    }

    @Test
    @DisplayName("내역을 조회시: 특정 유저의 저장된 내역을 반환한다")
    void chargeTest11() {
        // given
        long userId = 1L;
        // 가짜 데이터(fakeHistories) 생성
        List<PointHistory> fakeHistories = List.of(
                new PointHistory(1, userId, 1000, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2, userId, 2000, TransactionType.USE, System.currentTimeMillis())
        );
        // 스텁 설정
        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(fakeHistories);

        // when
        // 테스트 대상 메서드 실행
        List<PointHistory> result = pointService.getHistories(userId);

        // then
        // 결과 검증 (주장)
        assertEquals(2, result.size());
        assertEquals(TransactionType.CHARGE, result.get(0).type());
        assertEquals(TransactionType.USE, result.get(1).type());
    }

    @Test
    @DisplayName("내역 조회시 : 포인트 내역이 없는 경우 빈 리스트를 반환한다")
    void chargeTest12() {
        // given
        long userId = 1L;
        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(List.of());

        // when
        List<PointHistory> result = pointService.getHistories(userId);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("내역 조회시 : userId가 0 이하이면 에러 메시지를 던진다")
    void chargeTest13() {
        // given
        long userId = 0L;

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.getHistories(userId);
        });

        // then
        assertEquals("userId는 양수여야 합니다.", exception.getMessage());
    }
}
