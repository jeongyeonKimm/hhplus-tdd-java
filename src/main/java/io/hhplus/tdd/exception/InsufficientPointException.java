package io.hhplus.tdd.exception;

public class InsufficientPointException extends PointException {

    public InsufficientPointException(long currentPoint, long useAmount) {
        super("보유한 포인트를 초과합니다. 현재: " + currentPoint + ", 사용: " + useAmount);
    }
}
