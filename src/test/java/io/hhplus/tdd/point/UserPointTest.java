package io.hhplus.tdd.point;

import io.hhplus.tdd.exception.NegativePointAmountException;
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
    
    @DisplayName("유저 포인트 생성 시 포인트 값은 음수이면 NegativePointAmountException이 발생한다.")
    @Test
    void constructor_shouldThrowException_whenPointIsNegative() {
        long negativePoint = -100L;

        assertThatThrownBy(() -> new UserPoint(1L, negativePoint, System.currentTimeMillis()))
                .isInstanceOf(NegativePointAmountException.class)
                .hasMessage("포인트는 음수가 될 수 없습니다.");
    }

    @DisplayName("0 이하의 포인트을 사용하려고 하면 NonPositiveUseAmountException이 발생한다.")
    @Test
    void use_shouldThrowNonPositiveUseAmountException_whenAmountIsZeroOrNegative() {
        UserPoint userPoint = new UserPoint(1L, 1000L, System.currentTimeMillis());

        assertThatThrownBy(() -> userPoint.use(0L))
                .isInstanceOf(NonPositiveUseAmountException.class)
                .hasMessage("사용 금액이 0 이하일 수 없습니다.");

        assertThatThrownBy(() -> userPoint.use(-200L))
                .isInstanceOf(NonPositiveUseAmountException.class)
                .hasMessage("사용 금액이 0 이하일 수 없습니다.");
    }

    @DisplayName("사용하려고 하는 포인트가 보유 중인 포인트를 초과하면 InsufficientPointException이 발생한다.")
    @Test
    void use_shouldThrowInsufficientPointException_whenInsufficientPoint() {
        long currentPoint = 500_000L;
        long useAmount = 700_000L;
        UserPoint userPoint = new UserPoint(1L, currentPoint, System.currentTimeMillis());

        assertThatThrownBy(() -> userPoint.use(useAmount))
                .isInstanceOf(InsufficientPointException.class)
                .hasMessage("보유한 포인트를 초과합니다. 현재: " + currentPoint + ", 사용: " + useAmount);
    }

    @DisplayName("보유 중인 포인트 한도 내에서 0보다 큰 금액을 사용하려는 경우 정상적으로 포인트 사용이 된다.")
    @Test
    void use() {
        long currentPoint = 500_000L;
        long useAmount = 300_000L;
        UserPoint userPoint = new UserPoint(1L, currentPoint, System.currentTimeMillis());

        UserPoint afterUse = userPoint.use(useAmount);

        long expectedAmount = userPoint.point() - useAmount;
        assertThat(afterUse.id()).isEqualTo(userPoint.id());
        assertThat(afterUse.point()).isEqualTo(expectedAmount);
    }
}
