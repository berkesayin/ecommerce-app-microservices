package dev.berke.app.shared.handler;

import dev.berke.app.shared.exception.CustomerServiceCommunicationException;
import dev.berke.app.shared.exception.EmailAlreadyExistsException;
import dev.berke.app.shared.exception.InvalidTokenException;
import dev.berke.app.shared.exception.RoleNotFoundException;
import dev.berke.app.shared.exception.UsernameAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({
            UsernameAlreadyExistsException.class,
            EmailAlreadyExistsException.class
    })
    ProblemDetail handleDuplicateResourceException(RuntimeException ex) {
        ProblemDetail problemDetail = ProblemDetail
                .forStatusAndDetail(
                        HttpStatus.CONFLICT,
                        ex.getMessage()
                );

        problemDetail.setTitle("Resource Conflict");
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(InvalidTokenException.class)
    ProblemDetail handleInvalidTokenException(InvalidTokenException ex) {
        ProblemDetail problemDetail = ProblemDetail
                .forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage()
                );

        problemDetail.setTitle("Invalid Token");
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(BadCredentialsException.class)
    ProblemDetail handleBadCredentialsException(BadCredentialsException ex) {
        ProblemDetail problemDetail = ProblemDetail
                .forStatusAndDetail(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid username or password."
                );

        problemDetail.setTitle("Authentication Failed");
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(RoleNotFoundException.class)
    ProblemDetail handleRoleNotFoundException(RoleNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail
                .forStatusAndDetail(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        ex.getMessage()
                );

        problemDetail.setTitle("Configuration Error");
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler({CustomerServiceCommunicationException.class})
    ProblemDetail handleServerExceptions(RuntimeException ex) {
        log.error("Server Error: ", ex);
        ProblemDetail problemDetail = ProblemDetail
                .forStatusAndDetail(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        ex.getMessage()
                );

        problemDetail.setTitle("Internal Server Error");
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    // common infrastructure exceptions

    // validation errors (@Valid)
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ProblemDetail problemDetail = ProblemDetail
                .forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        "Validation failed for request arguments"
                );

        problemDetail.setTitle("Validation Error");

        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        problemDetail.setProperty("errors", errors);
        problemDetail.setProperty("timestamp", Instant.now());

        return createResponseEntity(problemDetail, headers, status, request);
    }

    // json parsing errors
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ProblemDetail problemDetail = ProblemDetail
                .forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        "JSON request is not correct."
                );

        problemDetail.setTitle("JSON Parse Error");
        problemDetail.setProperty("timestamp", Instant.now());

        return createResponseEntity(problemDetail, headers, status, request);
    }

    // unexpected 500 errors
    @ExceptionHandler(Exception.class)
    ProblemDetail handleGlobalException(Exception ex) {
        log.error("An unexpected error occurred: ", ex);

        ProblemDetail problemDetail = ProblemDetail
                .forStatusAndDetail(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred."
                );

        problemDetail.setTitle("Internal Server Error");
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }
}
