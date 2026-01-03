package dev.berke.app.shared.handler;

import dev.berke.app.shared.exception.AddressNotFoundException;
import dev.berke.app.shared.exception.CustomerAlreadyExistsException;
import dev.berke.app.shared.exception.CustomerNotFoundException;
import dev.berke.app.shared.exception.InvalidRequestException;
import dev.berke.app.shared.exception.NoActiveAddressFoundException;
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
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // 1. resource not found (customer, address)
    @ExceptionHandler({
            CustomerNotFoundException.class,
            AddressNotFoundException.class,
            NoActiveAddressFoundException.class
    })
    ProblemDetail handleResourceNotFoundException(RuntimeException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());

        problemDetail.setTitle("Resource Not Found");
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    // 2. customer exists
    @ExceptionHandler(CustomerAlreadyExistsException.class)
    ProblemDetail handleProductAlreadyExistsException(CustomerAlreadyExistsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, ex.getMessage());

        problemDetail.setTitle("Customer Already Exists");
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(InvalidRequestException.class)
    ProblemDetail handleInvalidRequestException(InvalidRequestException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage());

        problemDetail.setTitle("Invalid Request");
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    // 4. unexpected errors (Unchanged)
    @ExceptionHandler(Exception.class)
    ProblemDetail handleGlobalException(Exception ex) {
        // It's good practice to log the full exception here
        // log.error("An unexpected error occurred: ", ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");

        problemDetail.setTitle("Internal Server Error");
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    // 5. validation errors (@Valid) (Unchanged)
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

    // 6. json parsing errors (Unchanged)
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
}