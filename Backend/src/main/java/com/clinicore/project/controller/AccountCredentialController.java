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

    // Login credentials
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserProfile loginDetails) {
        UserProfile userProfile = accountCredentialRepository.findByUsernameAndPasswordHash(loginDetails.getUsername(), loginDetails.getPasswordHash());
        if (userProfile != null) {
            // Return the ID so frontend can access user/resident data
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("id", userProfile.getId());
            response.put("username", userProfile.getUsername());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials");
    }

    // Registering a new user
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserProfile newUser) {
        try{
            accountCredentialRepository.save(newUser);
            return ResponseEntity.ok("Account Created");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to Register User");
        }
    }

}
