package com.trainingmanagement.model;

import java.time.LocalDateTime;

/**
 * JSON body returned for every error, e.g.
 * { "status": 409, "error": "Conflict", "message": "This email is already used", "timestamp": "..." }
 * Stack traces are never sent to the browser.
 */
public class ApiError {

    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp = LocalDateTime.now();

    public ApiError(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
