package io.hhplus.tdd.point;

import io.hhplus.tdd.exception.NonPositiveChargeAmountException;
import io.hhplus.tdd.exception.PointLimitExceededException;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    private static final long MAX_POINT = 1_000_000L;

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public UserPoint charge(long amount) {
        if (amount <= 0) {
            throw new NonPositiveChargeAmountException();
        }

        long newPoint = point + amount;
        if (newPoint > MAX_POINT) {
            throw new PointLimitExceededException(point, amount, MAX_POINT);
        }

        return new UserPoint(id, newPoint, System.currentTimeMillis());
    }
}
