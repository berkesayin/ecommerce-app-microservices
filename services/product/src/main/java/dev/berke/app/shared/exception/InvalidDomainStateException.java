package dev.berke.app.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidDomainStateException extends RuntimeException {

    public InvalidDomainStateException(String message) {
        super(message);
    }
}
