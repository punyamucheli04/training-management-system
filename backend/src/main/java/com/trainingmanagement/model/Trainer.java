package com.trainingmanagement.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A row of the <b>trainer</b> table.
 *
 * JSON property names are exactly the MySQL column names (e.g. "Trainer_ID"),
 * so the database, the REST API and the React code all use the same names.
 */
public class Trainer {

    @JsonProperty("Trainer_ID")
    private Integer trainerId;

    @JsonProperty("Trainer_Name")
    private String trainerName;

    @JsonProperty("Email")
    private String email;

    @JsonProperty("Expertise")
    private String expertise;

    /** subquery COUNT - read only */
    @JsonProperty("Session_Count")
    private Integer sessionCount;

    public Trainer() {
    }

    public Integer getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(Integer trainerId) {
        this.trainerId = trainerId;
    }

    public String getTrainerName() {
        return trainerName;
    }

    public void setTrainerName(String trainerName) {
        this.trainerName = trainerName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getExpertise() {
        return expertise;
    }

    public void setExpertise(String expertise) {
        this.expertise = expertise;
    }

    public Integer getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(Integer sessionCount) {
        this.sessionCount = sessionCount;
    }
}
