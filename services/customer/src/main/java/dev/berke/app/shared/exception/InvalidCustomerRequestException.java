package dev.berke.app.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidCustomerRequestException extends RuntimeException {

    public InvalidCustomerRequestException(String message) {
        super(message);
    }
}