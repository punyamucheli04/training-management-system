package com.trainingmanagement.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * The logged-in user, as returned to the frontend and kept in the HTTP session.
 * The password hash is deliberately NOT part of this class, so it can never be
 * sent to the browser.
 */
public class UserAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("User_ID")
    private Integer userId;

    @JsonProperty("Username")
    private String username;

    @JsonProperty("Email")
    private String email;

    @JsonProperty("Role")
    private String role;

    public UserAccount() {
    }

    public UserAccount(Integer userId, String username, String email, String role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
