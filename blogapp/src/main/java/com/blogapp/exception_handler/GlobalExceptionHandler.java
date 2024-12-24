package com.blogapp.exception_handler;
import com.blogapp.exception.ImageUploadException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import com.blogapp.exception.CategoryAlreadyExistsException;
import com.blogapp.exception.ImagesLimitExceedException;
import com.blogapp.exception.UserAlreadyExistsException;
import com.blogapp.payload.ErrorDetails;
import com.blogapp.payload.ValidationError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ImagesLimitExceedException.class)
    public ResponseEntity<ErrorDetails> handleImagesNotAcceptedException(
            ImagesLimitExceedException e,
            WebRequest request
    ){
        ErrorDetails getDetails = new ErrorDetails(
                new Date(),
                e.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(getDetails, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(CategoryAlreadyExistsException.class)
    public ResponseEntity<ErrorDetails> handleCategoryException(
            CategoryAlreadyExistsException e,
            WebRequest request
    ){
        ErrorDetails getDetails = new ErrorDetails(
                new Date(),
                e.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(getDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorDetails> handleUserException(
            UserAlreadyExistsException e,
            WebRequest request
    ){
        ErrorDetails getDetails = new ErrorDetails(
                new Date(),
                e.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(getDetails, HttpStatus.INTERNAL_SERVER_ERROR);
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
    public ResponseEntity<ErrorDetails> handleImageUploadException(
            ImageUploadException e,
            WebRequest request
    ){
        ErrorDetails getDetails = new ErrorDetails(
                new Date(),
                e.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(getDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(
            Exception e,
            WebRequest request
    ){
        ErrorDetails getDetails = new ErrorDetails(
                new Date(),
                e.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(getDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
