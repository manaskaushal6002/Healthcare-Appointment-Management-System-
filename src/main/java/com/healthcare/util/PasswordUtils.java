package com.healthcare.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Utility for cryptographic salt generation and SHA-256 password hashing.
 * Passwords are never stored in plain text.
 */
public final class PasswordUtils {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALGORITHM = "SHA-256";

    private PasswordUtils() {
        // Utility class, no instantiation
    }

    /**
     * Generates a secure random 32-character hex salt (16 bytes).
     */
    public static String generateSalt() {
        byte[] saltBytes = new byte[16];
        RANDOM.nextBytes(saltBytes);
        return bytesToHex(saltBytes);
    }

    /**
     * Hashes the raw password with the provided salt using SHA-256.
     * Order: salt bytes followed by password bytes.
     *
     * @param password raw user password
     * @param salt user-specific cryptographic salt
     * @return 64-character hex hash string
     */
    public static String hashPassword(String password, String salt) {
        if (password == null || salt == null) {
            throw new IllegalArgumentException("Password and salt must not be null.");
        }
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashedBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Cryptographic algorithm " + ALGORITHM + " not found.", e);
        }
    }

    /**
     * Verifies raw password against the stored salt and hash in constant time.
     */
    public static boolean verifyPassword(String rawPassword, String salt, String expectedHash) {
        if (rawPassword == null || salt == null || expectedHash == null) {
            return false;
        }
        String computedHash = hashPassword(rawPassword, salt);
        return MessageDigest.isEqual(
                computedHash.getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
