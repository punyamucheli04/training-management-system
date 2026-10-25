package com.trainingmanagement.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Checks that a username is locked after too many wrong passwords. */
class LoginAttemptServiceTest {

    @Test
    void lockedAfterFiveFailures() {
        LoginAttemptService service = new LoginAttemptService();
        for (int i = 0; i < 4; i++) {
            service.loginFailed("admin");
        }
        assertEquals(0L, service.minutesLocked("admin"));
        service.loginFailed("ADMIN"); // usernames are compared without case
        assertEquals(15L, service.minutesLocked("admin"));
    }

    @Test
    void successResetsTheCounter() {
        LoginAttemptService service = new LoginAttemptService();
        for (int i = 0; i < 4; i++) {
            service.loginFailed("teacher");
        }
        service.loginSucceeded("teacher");
        service.loginFailed("teacher");
        assertEquals(0L, service.minutesLocked("teacher"));
    }
}
