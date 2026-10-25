package com.trainingmanagement.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * One row returned by the stored procedure GetStudentAttendance.
 *
 * JSON property names are exactly the MySQL column names (e.g. "Session_ID"),
 * so the database, the REST API and the React code all use the same names.
 */
public class AttendanceRecord {

    @JsonProperty("Session_ID")
    private Integer sessionId;

    @JsonProperty("Session_Date")
    private LocalDate sessionDate;

    @JsonProperty("Start_Time")
    private LocalTime startTime;

    @JsonProperty("End_Time")
    private LocalTime endTime;

    @JsonProperty("Tr_Name")
    private String trName;

    @JsonProperty("Trainer_Name")
    private String trainerName;

    @JsonProperty("Attendance_Status")
    private String attendanceStatus;

    public AttendanceRecord() {
    }

    public Integer getSessionId() {
        return sessionId;
    }

    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
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
}
