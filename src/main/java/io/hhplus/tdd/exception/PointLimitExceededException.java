package io.hhplus.tdd.exception;

public class PointLimitExceededException extends RuntimeException {

    public PointLimitExceededException(long currentPoint, long chargeAmount, long maxAmount) {
        super("최대 포인트 한도를 초과합니다. 현재: " + currentPoint + ", 충전: " + chargeAmount + ", 최대: " + maxAmount);
    }
}
