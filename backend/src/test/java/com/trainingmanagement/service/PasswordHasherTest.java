package com.trainingmanagement.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Checks the password hashing used by the login (no database needed). */
class PasswordHasherTest {

    @Test
    void hashedPasswordMatchesOnlyTheRightPassword() {
        String hash = PasswordHasher.hash("secret99");
        assertEquals(true, hash.startsWith("pbkdf2_sha256$"));
        assertEquals(true, PasswordHasher.matches("secret99", hash));
        assertEquals(false, PasswordHasher.matches("secret98", hash));
    }

    @Test
    void samePasswordGivesDifferentHashesBecauseOfSalt() {
        assertEquals(false, PasswordHasher.hash("abc123").equals(PasswordHasher.hash("abc123")));
    }

    @Test
    void defaultAdminHashInUsersSqlIsAdmin123() {
        String hashInUsersSql = "pbkdf2_sha256$120000$nyrDk0ccHo4m8HrlkRtQzg==$2rpkoTNgp/KI0MzGgsltnqllbzxWivqKhUaRyB8x4Bo=";
        assertEquals(true, PasswordHasher.matches("admin123", hashInUsersSql));
        assertEquals(false, PasswordHasher.matches("Admin123", hashInUsersSql));
    }

    @Test
    void olderSha256AndPlainTextRowsStillWork() {
        // SHA2('admin123', 256) in MySQL
        String sha = "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9";
        assertEquals(true, PasswordHasher.matches("admin123", sha));
        assertEquals(true, PasswordHasher.matches("plain", "plain"));
        assertEquals(false, PasswordHasher.matches("x", null));
    }
}
