/**
 * Olei Amelie Ngan
 *
 * Service layer for account credentials (username, password)
 * This service layer uses the account credential repository to authenticate users and create new user accounts
 *
 * Functions/Purposes:
 * - Authenticate user with username and password
 * - Validate roles and convert role string to enum
 */
package com.clinicore.project.service;

import com.clinicore.project.entity.UserProfile;
import com.clinicore.project.repository.AccountCredentialRepository;
import com.clinicore.project.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import java.util.*;
import com.clinicore.project.service.EmailService;
import org.springframework.beans.factory.annotation.Value;

@Service
public class AccountCredentialService {

    private final AccountCredentialRepository accountCredentialRepository;
    private final UserProfileRepository userProfileRepository;
    private final EmailService emailService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public AccountCredentialService(AccountCredentialRepository accountCredentialRepository,
                                    UserProfileRepository userProfileRepository,
                                    EmailService emailService) {
        this.accountCredentialRepository = accountCredentialRepository;
        this.userProfileRepository = userProfileRepository;
        this.emailService = emailService;
    }

    // authenticate user with username and password
    public Map<String, Object> authenticateUser(String username, String passwordHash) {

        // find given user in db
        UserProfile userProfile = accountCredentialRepository.findByUsernameAndPasswordHash(username, passwordHash);
        
        if (userProfile == null) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        // build and return authentication response on success
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


    public boolean checkIfUserExistsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        return userProfileRepository.findByEmail(email).isPresent();
    }

    public String getUsernameByEmail(String email) {
        return userProfileRepository.findByEmail(email)
                .map(user -> user.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Email not found"));
    }

    public void sendPasswordReset(String email) {
        // verify email exists first
        if (!userProfileRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email not found");
        }

        // encode email to be safe in URL
        String encodedEmail = java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8);
        String resetLink = frontendUrl + "/reset-password?email=" + encodedEmail;
        emailService.sendPasswordResetLink(email, resetLink);
    }

    public void resetPassword(String email, String newPassword) {
        UserProfile user = userProfileRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email not found"));

        user.setPasswordHash(newPassword);
        userProfileRepository.save(user);
    }
}