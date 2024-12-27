package com.blogapp.exception;

public class CommentNotFountException extends RuntimeException {
    public CommentNotFountException(String message) {
        super(message);
    }
}
