package com.example.capstone3.Advice;

import com.example.capstone3.Api.ApiException;
import com.example.capstone3.Api.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.StaleObjectStateException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.InputMismatchException;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<?> APIException(ApiException apiException) {
        String message = apiException.getMessage();
        return ResponseEntity.status(400).body(new ApiResponse(message));
    }

    // duplicate
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<?> SQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException e) {
        return ResponseEntity.status(400).body(new ApiResponse("The request conflicts with existing data"));
    }

    // id entering when its generated
    @ExceptionHandler(StaleObjectStateException.class)
    public ResponseEntity<?> StaleObjectStateException(StaleObjectStateException e) {
        return ResponseEntity.status(400).body(new ApiResponse("The record was changed by another operation"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> MethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getFieldError().getDefaultMessage();
        return ResponseEntity.status(400).body(new ApiResponse(message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> HttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return ResponseEntity.status(400).body(new ApiResponse("Request body is invalid or contains an unsupported value"));
    }

    @ExceptionHandler(InputMismatchException.class)
    public ResponseEntity<?> InputMismatchException(InputMismatchException e) {
        return ResponseEntity.status(400).body(new ApiResponse("Request value has an invalid format"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> NoResourceFoundException(NoResourceFoundException e) {
        return ResponseEntity.status(400).body(new ApiResponse("Requested resource was not found"));
    }

    // Server Validation Exception
    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResponseEntity<ApiResponse> ConstraintViolationException(ConstraintViolationException e) {
        String msg = e.getMessage();
        return ResponseEntity.status(400).body(new ApiResponse(msg));
    }


    // wrong write SQL in @column Exception
    @ExceptionHandler(value = InvalidDataAccessResourceUsageException.class)
    public ResponseEntity<ApiResponse> InvalidDataAccessResourceUsageException(InvalidDataAccessResourceUsageException e) {
        return ResponseEntity.status(500).body(new ApiResponse("A database operation could not be completed"));
    }

    // Database Constraint Exception
    @ExceptionHandler(value = DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> DataIntegrityViolationException(DataIntegrityViolationException e) {
        return ResponseEntity.status(400).body(new ApiResponse("The request conflicts with existing data"));
    }

    // Method not allowed Exception
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse> HttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(400).body(new ApiResponse("HTTP method is not supported for this endpoint"));
    }

    // TypesMisMatch Exception
    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse> MethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.status(400).body(new ApiResponse("Request parameter has an invalid value"));
    }

    @ExceptionHandler(HttpClientErrorException.TooManyRequests.class)
    public ResponseEntity<ApiResponse> handleTooManyRequests(HttpClientErrorException.TooManyRequests e) {
        return ResponseEntity.status(400).body(new ApiResponse("AI service is temporarily unavailable. Please try again later."));
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ApiResponse> handleRestClientException(RestClientException e) {
        return ResponseEntity.status(400).body(new ApiResponse("External service is temporarily unavailable. Please try again later."));
    }


}
