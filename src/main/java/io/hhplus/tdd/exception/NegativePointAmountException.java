package io.hhplus.tdd.exception;

public class NegativePointAmountException extends PointException {

    public NegativePointAmountException() {
        super("포인트는 음수가 될 수 없습니다.");
    }
}
