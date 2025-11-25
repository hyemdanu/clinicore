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

@Service
public class AccountCredentialService {

    // variables
    private final AccountCredentialRepository accountCredentialRepository;
    private final UserProfileRepository userProfileRepository;
    // constructor that injects all repositories so the service can access user and invitation data
    public AccountCredentialService(AccountCredentialRepository accountCredentialRepository,
                                    UserProfileRepository userProfileRepository) {
        this.accountCredentialRepository = accountCredentialRepository;
        this.userProfileRepository = userProfileRepository;
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

        // uses the new email column
        return userProfileRepository.findByEmail(email).isPresent();
    }
}