package com.trainingmanagement.exception;

/** Thrown when the request data is invalid (missing field, bad value...). Mapped to HTTP 400. */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
