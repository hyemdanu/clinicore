package com.clinicore.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Password Service
 * Handles all password hashing and verification using Argon2
 *
 * This service provides a clean interface for password operations
 * and should be used throughout the application whenever working
 * with passwords.
 */
@Service
public class PasswordService {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Hash a plain text password using Argon2
     *
     * Example:
     * String plainPassword = "MyPassword123!";
     * String hashed = passwordService.hashPassword(plainPassword);
     * // Result: "$argon2id$v=19$m=65536,t=3,p=1$..."
     *
     * @param plainPassword The plain text password to hash
     * @return The hashed password (safe to store in database)
     * @throws IllegalArgumentException if plainPassword is null or empty
     */
    public String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        return passwordEncoder.encode(plainPassword);
    }

    /**
     * Verify a plain text password against a hashed password
     *
     * Example:
     * String plainPassword = "MyPassword123!";
     * String hashedPassword = "$argon2id$v=19$m=65536,t=3,p=1$...";
     * boolean matches = passwordService.verifyPassword(plainPassword, hashedPassword);
     * // Result: true if passwords match, false otherwise
     *
     * @param plainPassword The plain text password to check
     * @param hashedPassword The hashed password from database
     * @return true if passwords match, false otherwise
     */
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }

        try {
            return passwordEncoder.matches(plainPassword, hashedPassword);
        } catch (Exception e) {
            // If hashing algorithm changed or hash is corrupted
            return false;
        }
    }

    /**
     * Check if a password needs to be rehashed
     *
     * This is useful when you upgrade the hashing algorithm parameters.
     * If this returns true, you should rehash the password on next login.
     *
     * Example:
     * if (passwordService.needsRehash(user.getPassword())) {
     *     String newHash = passwordService.hashPassword(plainPassword);
     *     user.setPassword(newHash);
     *     userRepository.save(user);
     * }
     *
     * @param hashedPassword The hashed password to check
     * @return true if password should be rehashed with new parameters
     */
    public boolean needsRehash(String hashedPassword) {
        if (hashedPassword == null) {
            return true;
        }

        try {
            return passwordEncoder.upgradeEncoding(hashedPassword);
        } catch (Exception e) {
            // If hash format is unrecognized, it needs rehashing
            return true;
        }
    }
}