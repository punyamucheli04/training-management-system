package com.trainingmanagement.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request body for POST /api/enrollments.
 *
 * JSON property names are exactly the MySQL column names (e.g. "Session_ID"),
 * so the database, the REST API and the React code all use the same names.
 */
public class EnrollmentRequest {

    @JsonProperty("Session_ID")
    private Integer sessionId;

    @JsonProperty("Student_ID")
    private Integer studentId;

    /** optional, defaults to Pending */
    @JsonProperty("Attendance_Status")
    private String attendanceStatus;

    public EnrollmentRequest() {
    }

    public Integer getSessionId() {
        return sessionId;
    }

    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }
}
