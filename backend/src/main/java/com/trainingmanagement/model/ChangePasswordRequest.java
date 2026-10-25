package com.trainingmanagement.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Body of PUT /api/auth/password. */
public class ChangePasswordRequest {

    @JsonProperty("Current_Password")
    private String currentPassword;

    @JsonProperty("New_Password")
    private String newPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
