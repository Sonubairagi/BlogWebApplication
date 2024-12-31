package com.xplore.exception;

public class VerificationFailed extends RuntimeException {
    public VerificationFailed(String message) {
        super(message);
    }
}
