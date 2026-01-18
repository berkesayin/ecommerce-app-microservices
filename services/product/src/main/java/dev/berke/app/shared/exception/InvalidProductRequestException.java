package dev.berke.app.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidProductRequestException extends RuntimeException {

    public InvalidProductRequestException(String message) {
        super(message);
    }
}