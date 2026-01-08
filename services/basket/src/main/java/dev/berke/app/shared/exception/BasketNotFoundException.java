package dev.berke.app.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class BasketNotFoundException extends RuntimeException {

    public BasketNotFoundException(String message) {
        super(message);
    }
}
