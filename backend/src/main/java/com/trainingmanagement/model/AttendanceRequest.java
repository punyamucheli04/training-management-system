package com.trainingmanagement.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request body for PUT /api/enrollments/{sessionId}/{studentId}/attendance.
 *
 * JSON property names are exactly the MySQL column names (e.g. "Attendance_Status"),
 * so the database, the REST API and the React code all use the same names.
 */
public class AttendanceRequest {

    /** Present, Absent or Pending */
    @JsonProperty("Attendance_Status")
    private String attendanceStatus;

    public AttendanceRequest() {
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }
}
