package com.trainingmanagement.service;

import com.trainingmanagement.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the validation rules. They do not need MySQL, so
 * "mvn clean install" works even before the database is configured.
 */
class InputValidatorTest {

    @Test
    void attendanceStatusIsNormalised() {
        assertEquals("Present", InputValidator.attendanceStatus(" present "));
        assertEquals("Absent", InputValidator.attendanceStatus("ABSENT"));
        assertEquals("Pending", InputValidator.attendanceStatus("Pending"));
    }

    @Test
    void invalidAttendanceStatusIsRejected() {
        assertThrows(BadRequestException.class, () -> InputValidator.attendanceStatus("Late"));
        assertThrows(BadRequestException.class, () -> InputValidator.attendanceStatus(""));
    }

    @Test
    void requiredTextIsTrimmedAndChecked() {
        assertEquals("Rahul", InputValidator.requireText("  Rahul ", "Name", 50));
        assertThrows(BadRequestException.class, () -> InputValidator.requireText("   ", "Name", 50));
        assertThrows(BadRequestException.class, () -> InputValidator.requireText("x".repeat(51), "Name", 50));
    }

    @Test
    void blankOptionalTextBecomesNull() {
        assertNull(InputValidator.optionalText("  ", "HOD_Name", 50));
    }

    @Test
    void emailIsValidated() {
        assertEquals("rahul@college.edu", InputValidator.optionalEmail("Rahul@College.edu", "Email"));
        assertThrows(BadRequestException.class, () -> InputValidator.optionalEmail("not-an-email", "Email"));
    }

    @Test
    void cgpaRangeIsChecked() {
        assertEquals(new BigDecimal("8.50"), InputValidator.optionalRange(new BigDecimal("8.50"), "CGPA", 0, 10));
        assertThrows(BadRequestException.class,
                () -> InputValidator.optionalRange(new BigDecimal("10.5"), "CGPA", 0, 10));
    }
}
