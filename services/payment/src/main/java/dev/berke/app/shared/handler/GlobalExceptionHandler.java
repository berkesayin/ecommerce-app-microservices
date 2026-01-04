package dev.berke.app.shared.handler;

import dev.berke.app.shared.exception.CreditCardNotFoundException;
import dev.berke.app.shared.exception.InvalidRequestException;
import dev.berke.app.shared.exception.PaymentExecutionException;
import dev.berke.app.shared.exception.UpstreamDataException;
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

    @ExceptionHandler(CreditCardNotFoundException.class)
    ProblemDetail handleCreditCardNotFoundException(CreditCardNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());

        problemDetail.setTitle("Credit Card Not Found");
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(PaymentExecutionException.class)
    ProblemDetail handlePaymentExecutionException(PaymentExecutionException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.PAYMENT_REQUIRED, ex.getMessage());

        problemDetail.setTitle("CreditCard Failed");
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    // upstream data issues (customer, basket service issues)
    @ExceptionHandler(UpstreamDataException.class)
    ProblemDetail handleUpstreamDataException(UpstreamDataException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY, ex.getMessage()); // 502 Bad Gateway

        problemDetail.setTitle("Upstream Service Error");
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    // common infrastructure exceptions

    // business logic check
    @ExceptionHandler(InvalidRequestException.class)
    ProblemDetail handleInvalidRequestException(InvalidRequestException ex) {
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