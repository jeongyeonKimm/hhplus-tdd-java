package io.hhplus.tdd.point;

import io.hhplus.tdd.exception.NonPositiveChargeAmountException;
import io.hhplus.tdd.exception.PointLimitExceededException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserPointTest {

    private static final long MAX_POINT = 1_000_000L;

    @DisplayName("0 이하의 금액을 충전하려고 하면 NonPositiveChargeAmountException이 발생한다.")
    @Test
    void charge_shouldThrowNonPositiveChargeAmountException_whenAmountIsZeroOrNegative() {
        UserPoint userPoint = new UserPoint(1L, 1000L, System.currentTimeMillis());

        assertThatThrownBy(() -> userPoint.charge(0L))
                .isInstanceOf(NonPositiveChargeAmountException.class)
                .hasMessage("충전 금액이 0 이하일 수 없습니다.");

        assertThatThrownBy(() -> userPoint.charge(-2L))
                .isInstanceOf(NonPositiveChargeAmountException.class)
                .hasMessage("충전 금액이 0 이하일 수 없습니다.");
    }

    @DisplayName("충전할 금액과 이미 보유 중인 포인트의 합이 최대 보유 가능 포인트를 초과하면 PointLimitExceededException이 발생한다.")
    @Test
    void charge_shouldThrowPointLimitExceededException_whenTotalExceedsMax() {
        long currentPoint = 900_000L;
        long chargeAmount = 200_000L;
        UserPoint userPoint = new UserPoint(1L, currentPoint, System.currentTimeMillis());

        assertThatThrownBy(() -> userPoint.charge(chargeAmount))
                .isInstanceOf(PointLimitExceededException.class)
                .hasMessage("최대 포인트 한도를 초과합니다. 현재: " + currentPoint + ", 충전: " + chargeAmount + ", 최대: " + MAX_POINT);
    }

    @DisplayName("충전할 금액이 0을 초과하고 이미 보유 중인 포인트와의 합이 최대 보유 가능 포인트를 초과하지 않으면 포인트가 정상적으로 충전된다.")
    @Test
    void charge() {
        long currentPoint = 500_000L;
        long chargeAmount = 300_000L;
        UserPoint userPoint = new UserPoint(1L, currentPoint, System.currentTimeMillis());

        UserPoint afterCharge = userPoint.charge(chargeAmount);

        long expectedAmount = userPoint.point() + chargeAmount;
        assertThat(afterCharge.id()).isEqualTo(userPoint.id());
        assertThat(afterCharge.point()).isEqualTo(expectedAmount);
    }
}
