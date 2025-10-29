package com.clinicore.project.controller;

import com.clinicore.project.entity.UserProfile;
import com.clinicore.project.repository.AccountCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/accountCredential")
public class AccountCredentialController {
    @Autowired
    private AccountCredentialRepository accountCredentialRepository;

    /**
     * Login endpoint
     * Returns user ID, username, and role
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserProfile loginDetails) {
        UserProfile userProfile = accountCredentialRepository.findByUsernameAndPasswordHash(
                loginDetails.getUsername(), 
                loginDetails.getPasswordHash());
        
        if (userProfile != null) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("id", userProfile.getId());
            response.put("username", userProfile.getUsername());
            response.put("role", userProfile.getRole().toString());  // include role
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid Credentials");
    }

    /**
     * Register endpoint
     * Creates a new user with the specified role
     * If no role provided, defaults to RESIDENT
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserProfile newUser) {
        try {
            // Set role if not provided
            if (newUser.getRole() == null) {
                newUser.setRole(UserProfile.Role.RESIDENT);  // Default role
            }
            
            // Validate role is one of the allowed values
            accountCredentialRepository.save(newUser);
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("id", newUser.getId());
            response.put("message", "Account Created");
            response.put("role", newUser.getRole().toString());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid role provided"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to Register User: " + e.getMessage()));
        }
    }
}
