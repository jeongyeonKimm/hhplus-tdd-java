package io.hhplus.tdd.point;

import io.hhplus.tdd.exception.InsufficientPointException;
import io.hhplus.tdd.exception.NonPositiveChargeAmountException;
import io.hhplus.tdd.exception.NonPositiveUseAmountException;
import io.hhplus.tdd.exception.PointLimitExceededException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static io.hhplus.tdd.point.TransactionType.CHARGE;
import static io.hhplus.tdd.point.TransactionType.USE;
import static org.assertj.core.api.Assertions.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
class PointServiceIntegrationTest {

    private static final long MAX_POINT = 1_000_000L;

    @Autowired
    private PointService pointService;

    @Autowired
    private UserPointRepository userPointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @DisplayName("유저 아이디를 이용해 포인트를 조회한다.")
    @Test
    void getUserPoint() {
        long userId = 1L;
        userPointRepository.saveUserPoint(userId, 300_000L);

        UserPoint userPoint = pointService.getUserPoint(userId);

        assertThat(userPoint.point()).isEqualTo(300_000L);
    }

    @DisplayName("유저 아이디를 이용해 포인트 충전/사용 이력을 조회한다.")
    @Test
    void getPointHistories() {
        long userId = 1L;
        userPointRepository.saveUserPoint(userId, 300_000L);
        pointService.chargePoint(userId, 100_000L);
        pointService.usePoint(userId, 200_000L);

        List<PointHistory> histories = pointService.getUserPointHistory(userId);

        assertThat(histories).hasSize(2);
        assertThat(histories)
                .extracting("userId", "amount", "type")
                .containsExactlyInAnyOrder(
                        tuple(userId, 100_000L, CHARGE),
                        tuple(userId, 200_000L, USE)
                );
    }

    @DisplayName("0 이하의 금액을 충전하려고 하면 포인트 충전이 되지 않고 NonPositiveChargeAmountException이 발생한다.")
    @Test
    void chargePoint_shouldThrowNonPositiveChargeAmountException_whenAmountIsZeroOrNegative() {
        long userId = 1L;
        userPointRepository.saveUserPoint(userId, 800_000L);

        assertThatThrownBy(() -> pointService.chargePoint(userId, 0L))
                .isInstanceOf(NonPositiveChargeAmountException.class)
                .hasMessage("충전 금액이 0 이하일 수 없습니다.");

        List<PointHistory> histories = pointHistoryRepository.findPointHistoriesByUserId(userId);
        assertThat(histories.size()).isEqualTo(0);
    }

    @DisplayName("보유 중인 포인트에 충전할 금액을 누적한 값이 최대 보유 가능 포인트 범위를 넘어서면 포인트 충전이 되지 않고 PointLimitExceededException이 발생한다.")
    @Test
    void chargePoint_whenTotalExceedsMax() {
        long userId = 1L;
        long chargeAmount = 500_000L;
        UserPoint userPoint = userPointRepository.saveUserPoint(userId, 800_000L);

        assertThatThrownBy(() -> pointService.chargePoint(userId, chargeAmount))
                .isInstanceOf(PointLimitExceededException.class)
                .hasMessage("최대 포인트 한도를 초과합니다. 현재: " + userPoint.point() + ", 충전: " + chargeAmount + ", 최대: " + MAX_POINT);

        List<PointHistory> histories = pointHistoryRepository.findPointHistoriesByUserId(userId);
        assertThat(histories.size()).isEqualTo(0);
    }

    @DisplayName("0 보다 크고 누적 포인트가 최대 포인트 범위 이내인 금액을 충전한다.")
    @Test
    void chargePoint() {
        long userId = 1L;
        long chargeAmount = 100_000L;
        userPointRepository.saveUserPoint(userId, 300_000L);

        UserPoint chargedPoint = pointService.chargePoint(userId, chargeAmount);

        assertThat(chargedPoint.point()).isEqualTo(400_000L);

        UserPoint foundPoint = userPointRepository.findUserPointById(userId);
        assertThat(foundPoint.point()).isEqualTo(400_000L);

        List<PointHistory> pointHistories = pointHistoryRepository.findPointHistoriesByUserId(userId);
        assertThat(pointHistories).anyMatch(history -> history.amount() == chargeAmount && history.type() == CHARGE);
    }

    @DisplayName("0 이하의 금액을 사용하려고 하면 포인트 사용이 되지 않고 NonPositiveUseAmountException이 발생한다.")
    @Test
    void usePoint_shouldThrowNonPositiveUseAmountException_whenAmountIsZeroOrNegative() {
        long userId = 1L;
        userPointRepository.saveUserPoint(userId, 800_000L);

        assertThatThrownBy(() -> pointService.usePoint(userId, 0L))
                .isInstanceOf(NonPositiveUseAmountException.class)
                .hasMessage("사용 금액이 0 이하일 수 없습니다.");

        List<PointHistory> histories = pointHistoryRepository.findPointHistoriesByUserId(userId);
        assertThat(histories.size()).isEqualTo(0);
    }

    @DisplayName("사용하려고 하는 포인트가 보유 중인 포인트를 초과하면 InsufficientPointException이 발생한다.")
    @Test
    void usePoint_shouldThrowInsufficientPointException_whenInsufficientPoint() {
        long userId = 1L;
        long useAmount = 900_000L;
        UserPoint userPoint = userPointRepository.saveUserPoint(userId, 800_000L);

        assertThatThrownBy(() -> pointService.usePoint(userId, useAmount))
                .isInstanceOf(InsufficientPointException.class)
                .hasMessage("보유한 포인트를 초과합니다. 현재: " + userPoint.point() + ", 사용: " + useAmount);

        List<PointHistory> histories = pointHistoryRepository.findPointHistoriesByUserId(userId);
        assertThat(histories.size()).isEqualTo(0);
    }

    @DisplayName("0 보다 크고 보유 중인 포인트 이내의 범위의 포인트를 사용한다.")
    @Test
    void usePoint() {
        long userId = 1L;
        long useAmount = 200_000L;
        userPointRepository.saveUserPoint(userId, 300_000L);

        UserPoint usedPoint = pointService.usePoint(userId, useAmount);

        assertThat(usedPoint.point()).isEqualTo(100_000L);

        UserPoint foundPoint = userPointRepository.findUserPointById(userId);
        assertThat(foundPoint.point()).isEqualTo(100_000L);

        List<PointHistory> pointHistories = pointHistoryRepository.findPointHistoriesByUserId(userId);
        assertThat(pointHistories).anyMatch(history -> history.amount() == useAmount && history.type() == USE);
    }
}
