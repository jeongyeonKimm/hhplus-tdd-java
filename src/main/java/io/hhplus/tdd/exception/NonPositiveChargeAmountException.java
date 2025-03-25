package io.hhplus.tdd.exception;

public class NonPositiveChargeAmountException extends RuntimeException {

    public NonPositiveChargeAmountException() {
        super("충전 금액이 0 이하일 수 없습니다.");
    }
}
