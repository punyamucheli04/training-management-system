package com.trainingmanagement.service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Password hashing with PBKDF2 (built into Java, no extra library).
 *
 * Stored format in users.Password_Hash:
 *     pbkdf2_sha256$120000$<salt in Base64>$<hash in Base64>
 *
 * - A random SALT makes two equal passwords produce different hashes.
 * - 120 000 ITERATIONS make guessing passwords slow for an attacker.
 * - The plain password is never stored.
 *
 * Rows created before this application may contain a SHA-256 hex hash or a
 * plain-text password; those are still accepted so existing users can log in.
 */
public final class PasswordHasher {

    private static final String PREFIX = "pbkdf2_sha256";
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    public static String hash(String password) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] key = pbkdf2(password, salt, ITERATIONS);
        Base64.Encoder b64 = Base64.getEncoder();
        return PREFIX + "$" + ITERATIONS + "$" + b64.encodeToString(salt) + "$" + b64.encodeToString(key);
    }

    public static boolean matches(String password, String storedHash) {
        if (password == null || storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        if (storedHash.startsWith(PREFIX + "$")) {
            String[] parts = storedHash.split("\\$");
            if (parts.length != 4) {
                return false;
            }
            try {
                int iterations = Integer.parseInt(parts[1]);
                byte[] salt = Base64.getDecoder().decode(parts[2]);
                byte[] expected = Base64.getDecoder().decode(parts[3]);
                byte[] actual = pbkdf2(password, salt, iterations);
                return MessageDigest.isEqual(expected, actual); // constant-time comparison
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }
        // Older rows: SHA-256 hex (e.g. created with MySQL SHA2('pwd', 256)) or plain text
        if (storedHash.matches("(?i)[0-9a-f]{64}")) {
            return MessageDigest.isEqual(storedHash.toLowerCase().getBytes(StandardCharsets.UTF_8),
                    sha256Hex(password).getBytes(StandardCharsets.UTF_8));
        }
        return MessageDigest.isEqual(storedHash.getBytes(StandardCharsets.UTF_8),
                password.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] pbkdf2(String password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_BITS);
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("PBKDF2 is not available", ex);
        }
    }

    private static String sha256Hex(String text) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    /** Prints a hash for a password: java PasswordHasher.java mypassword */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java PasswordHasher.java <password>");
            return;
        }
        System.out.println(hash(args[0]));
    }
}
