package com.trainingmanagement.controller;

import com.trainingmanagement.config.AuthInterceptor;
import com.trainingmanagement.exception.UnauthorizedException;
import com.trainingmanagement.model.ChangePasswordRequest;
import com.trainingmanagement.model.LoginRequest;
import com.trainingmanagement.model.UserAccount;
import com.trainingmanagement.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Login / logout using an HTTP session.
 * After a successful login the user is stored in the session; the browser
 * keeps the session cookie (JSESSIONID) and sends it with every request.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public UserAccount login(@RequestBody LoginRequest body, HttpServletRequest request) {
        UserAccount user = authService.login(body.getUsername(), body.getPassword());

        // Start a fresh session for the logged-in user
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        request.getSession(true).setAttribute(AuthInterceptor.SESSION_USER, user);
        return user;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.noContent().build();
    }

    /** Who is logged in? Used by the frontend when the page is (re)loaded. */
    @GetMapping("/me")
    public UserAccount me(HttpServletRequest request) {
        return currentUser(request);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest body, HttpServletRequest request) {
        authService.changePassword(currentUser(request), body.getCurrentPassword(), body.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    private UserAccount currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object user = session == null ? null : session.getAttribute(AuthInterceptor.SESSION_USER);
        if (!(user instanceof UserAccount account)) {
            throw new UnauthorizedException("Please log in.");
        }
        return account;
    }
}
