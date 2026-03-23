/**
 * Olei Amelie Ngan
 *
 * Service layer for account credentials (username, password)
 * This service layer uses the account credential repository to authenticate users and create new user accounts
 *
 * Functions/Purposes:
 * - Authenticate user with username and password (NOW WITH ARGON2!)
 * - Validate roles and convert role string to enum
 */
package com.clinicore.project.service;

import com.clinicore.project.entity.UserProfile;
import com.clinicore.project.repository.AccountCredentialRepository;
import com.clinicore.project.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;

@Service
public class AccountCredentialService {

    private final AccountCredentialRepository accountCredentialRepository;
    private final UserProfileRepository userProfileRepository;
    private final EmailService emailService;
    private final PasswordService passwordService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public AccountCredentialService(AccountCredentialRepository accountCredentialRepository,
                                    UserProfileRepository userProfileRepository,
                                    EmailService emailService,
                                    PasswordService passwordService) {
        this.accountCredentialRepository = accountCredentialRepository;
        this.userProfileRepository = userProfileRepository;
        this.emailService = emailService;
        this.passwordService = passwordService;
    }

    // authenticate user with username and password using ARGON2
    public Map<String, Object> authenticateUser(String username, String plainPassword) {

        UserProfile userProfile = userProfileRepository.findByUsername(username).orElse(null);

        if (userProfile == null) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        String storedPassword = userProfile.getPasswordHash();
        boolean isValidPassword = false;

        //Support multiple password formats
        if (storedPassword.startsWith("$argon2") || storedPassword.startsWith("{argon2}")) {
            // Argon2 hash - verify properly
            isValidPassword = passwordService.verifyPassword(plainPassword, storedPassword);
        } else {
            // Plain text - compare directly
            isValidPassword = plainPassword.equals(storedPassword);

            // Auto-upgrade to Argon2 on successful login
            if (isValidPassword) {
                String hashed = passwordService.hashPassword(plainPassword);
                userProfile.setPasswordHash(hashed);
                userProfileRepository.save(userProfile);
            }
        }

        if (!isValidPassword) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        // Check if password needs rehashing (if algorithm parameters changed)
        if (passwordService.needsRehash(userProfile.getPasswordHash())) {
            String newHash = passwordService.hashPassword(plainPassword);
            userProfile.setPasswordHash(newHash);
            userProfileRepository.save(userProfile);
        }

        // Build and return authentication response on success
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", userProfile.getId());
        response.put("username", userProfile.getUsername());
        response.put("role", userProfile.getRole().toString());

        return response;
    }


    // is role string a valid enum type? If yes, return enum role
    public UserProfile.Role validateRole(String roleString) {

        // validate role string is not empty
        if (roleString == null || roleString.trim().isEmpty()) {
            throw new IllegalArgumentException("Role is required");
        }
        try {

            // convert role string to enum type
            return UserProfile.Role.valueOf(roleString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role. Use RESIDENT, CAREGIVER, or ADMIN");
        }
    }


    @Transactional(readOnly = true)
    public boolean checkIfUserExistsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        return userProfileRepository.findByEmail(email).isPresent();
    }

    @Transactional(readOnly = true)
    public String getUsernameByEmail(String email) {
        return userProfileRepository.findByEmail(email)
                .map(user -> user.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Email not found"));
    }

    public void sendPasswordReset(String email) {
        UserProfile user = userProfileRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email not found"));

        // generate a random token and store it with a 1-hour expiry
        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiresAt(LocalDateTime.now().plusHours(24));
        userProfileRepository.save(user);

        String resetLink = frontendUrl + "/reset-password?token=" + token;
        emailService.sendPasswordResetLink(email, resetLink);
    }

    public void resetPassword(String token, String newPassword) {
        UserProfile user = userProfileRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset link"));

        // check if the token has expired
        if (user.getPasswordResetTokenExpiresAt() == null
                || user.getPasswordResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reset link has expired. Please request a new one.");
        }

        // hash new password with Argon2
        String hashedPassword = passwordService.hashPassword(newPassword);
        user.setPasswordHash(hashedPassword);
        // clear the token so it can't be reused
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);
        userProfileRepository.save(user);
    }
}
