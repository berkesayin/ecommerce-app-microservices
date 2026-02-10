package dev.berke.app.shared.handler;

import dev.berke.app.shared.exception.CategoryNotFoundException;
import dev.berke.app.shared.exception.EventPublishingException;
import dev.berke.app.shared.exception.InvalidDomainStateException;
import dev.berke.app.shared.exception.InvalidProductRequestException;
import dev.berke.app.shared.exception.ProductAlreadyExistsException;
import dev.berke.app.shared.exception.ProductNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

    @ExceptionHandler({ProductNotFoundException.class, CategoryNotFoundException.class})
    ProblemDetail handleResourceNotFoundException(RuntimeException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());

        problemDetail.setTitle("Resource Not Found");
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(ProductAlreadyExistsException.class)
    ProblemDetail handleProductAlreadyExistsException(ProductAlreadyExistsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, ex.getMessage());

        problemDetail.setTitle("Product Already Exists");
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(InvalidDomainStateException.class)
    ProblemDetail handleInvalidDomainState(InvalidDomainStateException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage());

        problemDetail.setTitle("Invalid Domain State");
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(EventPublishingException.class)
    ProblemDetail handleEventPublishingException(EventPublishingException ex) {
        log.error("An unexpected error occurred: ", ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");

        problemDetail.setTitle("Event Publishing Error");
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    // common infrastructure exceptions

    // business logic check
    @ExceptionHandler(InvalidProductRequestException.class)
    ProblemDetail handleInvalidRequestException(InvalidProductRequestException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage());

        problemDetail.setTitle("Invalid Request");
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    // validation errors (@Valid)
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Validation failed for request arguments");

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
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
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

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");

        problemDetail.setTitle("Internal Server Error");
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }
}
