package com.clinicore.project.service;

import com.clinicore.project.entity.UserProfile;
import com.clinicore.project.repository.AccountCredentialRepository;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AccountCredentialService {

    private final AccountCredentialRepository accountCredentialRepository;
    private final InvitationService invitationService;

    public AccountCredentialService(AccountCredentialRepository accountCredentialRepository,
                                   InvitationService invitationService) {
        this.accountCredentialRepository = accountCredentialRepository;
        this.invitationService = invitationService;
    }

    /**
     * Authenticate user with username and password
     */
    public Map<String, Object> authenticateUser(String username, String passwordHash) {
        UserProfile userProfile = accountCredentialRepository.findByUsernameAndPasswordHash(username, passwordHash);
        
        if (userProfile == null) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        // Build and return authentication response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", userProfile.getId());
        response.put("username", userProfile.getUsername());
        response.put("role", userProfile.getRole().toString());
        
        return response;
    }

    /**
     * Validate and convert role string to enum
     */
    public UserProfile.Role validateRole(String roleString) {
        if (roleString == null || roleString.trim().isEmpty()) {
            throw new IllegalArgumentException("Role is required");
        }

        try {
            return UserProfile.Role.valueOf(roleString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role. Use RESIDENT, CAREGIVER, or ADMIN");
        }
    }

    /**
     * Validate invitation request
     */
    public void validateInvitationRequest(String email, String roleString) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (roleString == null || roleString.trim().isEmpty()) {
            throw new IllegalArgumentException("Role is required");
        }
    }
}