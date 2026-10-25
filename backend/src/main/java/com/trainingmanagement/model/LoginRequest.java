package com.trainingmanagement.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Body of POST /api/auth/login: { "Username": "admin", "Password": "..." } */
public class LoginRequest {

    @JsonProperty("Username")
    private String username;

    @JsonProperty("Password")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
