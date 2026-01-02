package dev.berke.app.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoActiveAddressFoundException extends RuntimeException {

    public NoActiveAddressFoundException(String message) {
        super(message);
    }
}
