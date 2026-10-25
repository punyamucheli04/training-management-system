package com.trainingmanagement.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A row of the <b>training</b> table.
 *
 * JSON property names are exactly the MySQL column names (e.g. "Training_ID"),
 * so the database, the REST API and the React code all use the same names.
 */
public class Training {

    @JsonProperty("Training_ID")
    private Integer trainingId;

    @JsonProperty("Tr_Name")
    private String trName;

    @JsonProperty("Tr_Type")
    private String trType;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("Duration_Hrs")
    private Integer durationHrs;

    /** GROUP BY COUNT - read only */
    @JsonProperty("Session_Count")
    private Integer sessionCount;

    /** GROUP BY COUNT - read only */
    @JsonProperty("Enrollment_Count")
    private Integer enrollmentCount;

    public Training() {
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

    public String getTrType() {
        return trType;
    }

    public void setTrType(String trType) {
        this.trType = trType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDurationHrs() {
        return durationHrs;
    }

    public void setDurationHrs(Integer durationHrs) {
        this.durationHrs = durationHrs;
    }

    public Integer getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(Integer sessionCount) {
        this.sessionCount = sessionCount;
    }

    public Integer getEnrollmentCount() {
        return enrollmentCount;
    }

    public void setEnrollmentCount(Integer enrollmentCount) {
        this.enrollmentCount = enrollmentCount;
    }
}
