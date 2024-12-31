package com.xplore.exception_handler;
import com.xplore.exception.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import com.xplore.payload.ValidationError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.xplore.payload.ApiErrorResponse;
import org.slf4j.MDC;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ImagesLimitExceedException.class)
    public ResponseEntity<ApiErrorResponse> handleImagesNotAcceptedException(
            ImagesLimitExceedException ex,
            HttpServletRequest request
    ){
        String correlationId = MDC.get("correlationId") != null ? MDC.get("correlationId") : "N/A";
        String apiName = request.getMethod() + " " + request.getRequestURI();

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                correlationId,
                apiName,
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Failed"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(CategoryAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleCategoryException(
            CategoryAlreadyExistsException ex,
            HttpServletRequest request
    ){
        String correlationId = MDC.get("correlationId") != null ? MDC.get("correlationId") : "N/A";
        String apiName = request.getMethod() + " " + request.getRequestURI();

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                correlationId,
                apiName,
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Failed"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleUserException(
            UserAlreadyExistsException ex,
            HttpServletRequest request
    ){
        String correlationId = MDC.get("correlationId") != null ? MDC.get("correlationId") : "N/A";
        String apiName = request.getMethod() + " " + request.getRequestURI();

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                correlationId,
                apiName,
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Failed"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationError> handleValidationExceptions(
            MethodArgumentNotValidException e,
            WebRequest request
    ){
        List<String> errors = e.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> ((FieldError) error).getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        ValidationError errorResponse = new ValidationError("Validation failed", errors,request.getDescription(false));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ImageUploadException.class)
    public ResponseEntity<ApiErrorResponse> handleImageUploadException(
            ImageUploadException ex,
            HttpServletRequest request
    ){
        String correlationId = MDC.get("correlationId") != null ? MDC.get("correlationId") : "N/A";
        String apiName = request.getMethod() + " " + request.getRequestURI();

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                correlationId,
                apiName,
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Failed"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFoundException(
            UserNotFoundException ex,
            HttpServletRequest request
    ){
        String correlationId = MDC.get("correlationId") != null ? MDC.get("correlationId") : "N/A";
        String apiName = request.getMethod() + " " + request.getRequestURI();

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                correlationId,
                apiName,
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Failed"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(CommentNotFountException.class)
    public ResponseEntity<ApiErrorResponse> handleCommentNotFountException(
            CommentNotFountException ex,
            HttpServletRequest request
    ){
        String correlationId = MDC.get("correlationId") != null ? MDC.get("correlationId") : "N/A";
        String apiName = request.getMethod() + " " + request.getRequestURI();

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                correlationId,
                apiName,
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Failed"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handlePostNotFoundException(
            PostNotFoundException ex,
            HttpServletRequest request
    ){
        String correlationId = MDC.get("correlationId") != null ? MDC.get("correlationId") : "N/A";
        String apiName = request.getMethod() + " " + request.getRequestURI();

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                correlationId,
                apiName,
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Failed"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleCategoryNotFoundException(
            CategoryNotFoundException ex,
            HttpServletRequest request
    ){
        String correlationId = MDC.get("correlationId") != null ? MDC.get("correlationId") : "N/A";
        String apiName = request.getMethod() + " " + request.getRequestURI();

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                correlationId,
                apiName,
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Failed"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(VerificationFailed.class)
    public ResponseEntity<ApiErrorResponse> handleGlobalException(
            VerificationFailed ex,
            HttpServletRequest request
    ){
        String correlationId = MDC.get("correlationId") != null ? MDC.get("correlationId") : "N/A";
        String apiName = request.getMethod() + " " + request.getRequestURI();

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                correlationId,
                apiName,
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Failed"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGlobalException(
            Exception ex,
            HttpServletRequest request
    ){
        String correlationId = MDC.get("correlationId") != null ? MDC.get("correlationId") : "N/A";
        String apiName = request.getMethod() + " " + request.getRequestURI();

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                correlationId,
                apiName,
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Failed"
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
