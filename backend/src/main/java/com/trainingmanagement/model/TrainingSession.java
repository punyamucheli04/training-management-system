package com.trainingmanagement.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * A row of the <b>training_session</b> table (read from session_details_view).
 *
 * JSON property names are exactly the MySQL column names (e.g. "Session_ID"),
 * so the database, the REST API and the React code all use the same names.
 */
public class TrainingSession {

    @JsonProperty("Session_ID")
    private Integer sessionId;

    @JsonProperty("Session_Date")
    private LocalDate sessionDate;

    @JsonProperty("Start_Time")
    private LocalTime startTime;

    @JsonProperty("End_Time")
    private LocalTime endTime;

    @JsonProperty("Training_ID")
    private Integer trainingId;

    @JsonProperty("Trainer_ID")
    private Integer trainerId;

    /** from training (JOIN) - read only */
    @JsonProperty("Tr_Name")
    private String trName;

    /** from training (JOIN) - read only */
    @JsonProperty("Tr_Type")
    private String trType;

    /** from trainer (JOIN) - read only */
    @JsonProperty("Trainer_Name")
    private String trainerName;

    /** read only */
    @JsonProperty("Enrolled_Count")
    private Integer enrolledCount;

    /** read only */
    @JsonProperty("Present_Count")
    private Integer presentCount;

    /** Completed / Today / Upcoming - read only */
    @JsonProperty("Session_Status")
    private String sessionStatus;

    public TrainingSession() {
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

    public Integer getTrainingId() {
        return trainingId;
    }

    public void setTrainingId(Integer trainingId) {
        this.trainingId = trainingId;
    }

    public Integer getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(Integer trainerId) {
        this.trainerId = trainerId;
    }

    public String getTrName() {
        return trName;
    }

    public void setTrName(String trName) {
        this.trName = trName;
    }

    public String getTrType() {
        return trType;
    }

    public void setTrType(String trType) {
        this.trType = trType;
    }

    public String getTrainerName() {
        return trainerName;
    }

    public void setTrainerName(String trainerName) {
        this.trainerName = trainerName;
    }

    public Integer getEnrolledCount() {
        return enrolledCount;
    }

    public void setEnrolledCount(Integer enrolledCount) {
        this.enrolledCount = enrolledCount;
    }

    public Integer getPresentCount() {
        return presentCount;
    }

    public void setPresentCount(Integer presentCount) {
        this.presentCount = presentCount;
    }

    public String getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionStatus(String sessionStatus) {
        this.sessionStatus = sessionStatus;
    }
}
