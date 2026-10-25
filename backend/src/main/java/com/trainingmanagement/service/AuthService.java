package com.trainingmanagement.service;

import com.trainingmanagement.exception.BadRequestException;
import com.trainingmanagement.exception.TooManyAttemptsException;
import com.trainingmanagement.exception.UnauthorizedException;
import com.trainingmanagement.model.UserAccount;
import com.trainingmanagement.repository.UserRepository;
import com.trainingmanagement.repository.UserRepository.UserRow;
import org.springframework.stereotype.Service;

/** Checks usernames and passwords against the users table. */
@Service
public class AuthService {

    private static final String WRONG_LOGIN = "Incorrect username or password.";

    private final UserRepository userRepository;
    private final LoginAttemptService loginAttempts;

    public AuthService(UserRepository userRepository, LoginAttemptService loginAttempts) {
        this.userRepository = userRepository;
        this.loginAttempts = loginAttempts;
    }

    public UserAccount login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new BadRequestException("Enter your username and password.");
        }
        long minutes = loginAttempts.minutesLocked(username);
        if (minutes > 0) {
            throw new TooManyAttemptsException("Too many wrong passwords. Try again in " + minutes + " minute(s).");
        }
        UserRow row = userRepository.findByUsername(username.trim()).orElse(null);
        if (row == null || !PasswordHasher.matches(password, row.passwordHash())) {
            // Same message for unknown user and wrong password, so usernames cannot be guessed
            loginAttempts.loginFailed(username);
            throw new UnauthorizedException(WRONG_LOGIN);
        }
        loginAttempts.loginSucceeded(username);
        return row.account();
    }

    public void changePassword(UserAccount user, String currentPassword, String newPassword) {
        UserRow row = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Your account no longer exists. Please log in again."));
        if (!PasswordHasher.matches(currentPassword, row.passwordHash())) {
            throw new BadRequestException("The current password is not correct.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new BadRequestException("The new password must have at least 6 characters.");
        }
        if (newPassword.length() > 100) {
            throw new BadRequestException("The new password must be at most 100 characters.");
        }
        userRepository.updatePasswordHash(row.account().getUserId(), PasswordHasher.hash(newPassword));
    }
}
