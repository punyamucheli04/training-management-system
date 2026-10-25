package com.trainingmanagement.service;

import com.trainingmanagement.exception.BadRequestException;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Simple, readable validation helpers used by the service classes.
 * Every failure throws BadRequestException -> HTTP 400 with a clear message.
 */
public final class InputValidator {

    public static final List<String> ATTENDANCE_STATUSES = List.of("Present", "Absent", "Pending");

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+\\-() ]{7,20}$");

    private InputValidator() {
    }

    /** Required text: trimmed, not empty, not longer than the column size. */
    public static String requireText(String value, String field, int maxLength) {
        String text = optionalText(value, field, maxLength);
        if (text == null) {
            throw new BadRequestException(field + " is required.");
        }
        return text;
    }

    /** Optional text: blank becomes NULL. */
    public static String optionalText(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String text = value.trim();
        if (text.length() > maxLength) {
            throw new BadRequestException(field + " must be at most " + maxLength + " characters.");
        }
        return text;
    }

    public static String optionalEmail(String value, String field) {
        String email = optionalText(value, field, 100);
        if (email != null && !EMAIL.matcher(email).matches()) {
            throw new BadRequestException(field + " is not a valid email address.");
        }
        return email == null ? null : email.toLowerCase();
    }

    public static String optionalPhone(String value, String field) {
        String phone = optionalText(value, field, 50);
        if (phone != null && !PHONE.matcher(phone).matches()) {
            throw new BadRequestException(field + " must contain 7-20 digits (spaces, +, - and brackets allowed).");
        }
        return phone;
    }

    public static <T> T require(T value, String field) {
        if (value == null) {
            throw new BadRequestException(field + " is required.");
        }
        return value;
    }

    public static Integer optionalRange(Integer value, String field, int min, int max) {
        if (value != null && (value < min || value > max)) {
            throw new BadRequestException(field + " must be between " + min + " and " + max + ".");
        }
        return value;
    }

    public static BigDecimal optionalRange(BigDecimal value, String field, double min, double max) {
        if (value != null && (value.doubleValue() < min || value.doubleValue() > max)) {
            throw new BadRequestException(field + " must be between " + min + " and " + max + ".");
        }
        return value;
    }

    /**
     * Accepts "present", " PRESENT " etc. and returns the canonical value
     * "Present" / "Absent" / "Pending". The database trigger performs the same
     * check, so invalid values are rejected even outside this application.
     */
    public static String attendanceStatus(String value) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("Attendance_Status is required (Present, Absent or Pending).");
        }
        String trimmed = value.trim();
        for (String allowed : ATTENDANCE_STATUSES) {
            if (allowed.equalsIgnoreCase(trimmed)) {
                return allowed;
            }
        }
        throw new BadRequestException("Invalid Attendance_Status '" + trimmed + "'. Allowed values: Present, Absent, Pending.");
    }
}
