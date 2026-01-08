package dev.berke.app.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
public class PaymentExecutionException extends RuntimeException{

    public PaymentExecutionException(String message) {
        super(message);
    }
}
