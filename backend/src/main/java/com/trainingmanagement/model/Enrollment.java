package com.trainingmanagement.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * A row of the <b>session_enrollment</b> table (read from enrollment_details_view).
 *
 * JSON property names are exactly the MySQL column names (e.g. "Session_ID"),
 * so the database, the REST API and the React code all use the same names.
 */
public class Enrollment {

    @JsonProperty("Session_ID")
    private Integer sessionId;

    @JsonProperty("Student_ID")
    private Integer studentId;

    @JsonProperty("Student_Name")
    private String studentName;

    @JsonProperty("Student_Email")
    private String studentEmail;

    @JsonProperty("Dept_Name")
    private String deptName;

    @JsonProperty("Session_Date")
    private LocalDate sessionDate;

    @JsonProperty("Start_Time")
    private LocalTime startTime;

    @JsonProperty("End_Time")
    private LocalTime endTime;

    @JsonProperty("Training_ID")
    private Integer trainingId;

    @JsonProperty("Tr_Name")
    private String trName;

    @JsonProperty("Trainer_Name")
    private String trainerName;

    @JsonProperty("Attendance_Status")
    private String attendanceStatus;

    @JsonProperty("Enrolled_At")
    private LocalDateTime enrolledAt;

    public Enrollment() {
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

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getTrainingId() {
        return trainingId;
    }

    public void setTrainingId(Integer trainingId) {
        this.trainingId = trainingId;
    }

    public String getTrName() {
        return trName;
    }

    public void setTrName(String trName) {
        this.trName = trName;
    }

    public String getTrainerName() {
        return trainerName;
    }

    public void setTrainerName(String trainerName) {
        this.trainerName = trainerName;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }
}
