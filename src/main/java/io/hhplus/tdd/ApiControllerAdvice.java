package io.hhplus.tdd;

import io.hhplus.tdd.exception.InsufficientPointException;
import io.hhplus.tdd.exception.NonPositiveChargeAmountException;
import io.hhplus.tdd.exception.NonPositiveUseAmountException;
import io.hhplus.tdd.exception.PointLimitExceededException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
class ApiControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {
            InsufficientPointException.class,
            NegativeArraySizeException.class,
            NonPositiveChargeAmountException.class,
            NonPositiveUseAmountException.class,
            PointLimitExceededException.class}
    )
    public ResponseEntity<ErrorResponse> handleCustomException(Exception e) {
        return ResponseEntity.status(400)
                .body(new ErrorResponse("400", e.getMessage()));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity.status(500).body(new ErrorResponse("500", "에러가 발생했습니다."));
    }
}
