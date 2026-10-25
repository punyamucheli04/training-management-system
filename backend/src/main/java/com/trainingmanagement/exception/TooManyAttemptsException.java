package com.trainingmanagement.exception;

/** Thrown when a username is temporarily locked after too many wrong passwords. Mapped to HTTP 429. */
public class TooManyAttemptsException extends RuntimeException {

    public TooManyAttemptsException(String message) {
        super(message);
    }
}
