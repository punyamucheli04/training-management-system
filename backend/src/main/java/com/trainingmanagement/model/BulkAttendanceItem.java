package com.trainingmanagement.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * One student's status inside a bulk attendance update.
 *
 * JSON property names are exactly the MySQL column names (e.g. "Student_ID"),
 * so the database, the REST API and the React code all use the same names.
 */
public class BulkAttendanceItem {

    @JsonProperty("Student_ID")
    private Integer studentId;

    @JsonProperty("Attendance_Status")
    private String attendanceStatus;

    public BulkAttendanceItem() {
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
