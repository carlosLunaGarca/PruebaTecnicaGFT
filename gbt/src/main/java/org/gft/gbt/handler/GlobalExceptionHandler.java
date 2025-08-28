package org.gft.gbt.handler;

import org.gft.gbt.exception.FundNotFoundException;
import org.gft.gbt.exception.InsufficientBalanceException;
import org.gft.gbt.exception.SubscriptionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FundNotFoundException.class)
    public ResponseEntity<String> handleFundNotFound(FundNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler({InsufficientBalanceException.class, SubscriptionNotFoundException.class})
    public ResponseEntity<String> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
