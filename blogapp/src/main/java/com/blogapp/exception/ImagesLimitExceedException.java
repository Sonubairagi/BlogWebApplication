package com.blogapp.exception;

public class ImagesLimitExceedException extends RuntimeException {
    public ImagesLimitExceedException(String message) {
        super(message);
    }
}
