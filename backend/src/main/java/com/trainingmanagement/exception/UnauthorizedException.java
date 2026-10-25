package com.trainingmanagement.exception;

/** Thrown when the user is not logged in or the login is wrong. Mapped to HTTP 401. */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
