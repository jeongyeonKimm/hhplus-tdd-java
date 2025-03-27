package io.hhplus.tdd.exception;

public class NonPositiveUseAmountException extends PointException {

    public NonPositiveUseAmountException() {
        super("사용 금액이 0 이하일 수 없습니다.");
    }
}
