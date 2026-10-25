package com.trainingmanagement.exception;

/** Thrown when the request conflicts with existing data (duplicate, row in use...). Mapped to HTTP 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
