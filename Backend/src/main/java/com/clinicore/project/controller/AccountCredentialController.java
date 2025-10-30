package com.clinicore.project.controller;

import com.clinicore.project.entity.UserProfile;
import com.clinicore.project.service.AccountCredentialService;
import com.clinicore.project.service.InvitationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/accountCredential")
public class AccountCredentialController {

    private final AccountCredentialService accountCredentialService;
    private final InvitationService invitationService;

    // Inject service layers
    public AccountCredentialController(AccountCredentialService accountCredentialService,
                                      InvitationService invitationService) {
        this.accountCredentialService = accountCredentialService;
        this.invitationService = invitationService;
    }

    /**
     * Login endpoint
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserProfile loginDetails) {
        try {
            Map<String, Object> response = accountCredentialService.authenticateUser(
                    loginDetails.getUsername(),
                    loginDetails.getPasswordHash()
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Login failed: " + e.getMessage()));
        }
    }

    /**
     * Create invitation (ADMIN ONLY)
     */
    @PostMapping("/invite")
    public ResponseEntity<?> createInvitation(@RequestBody Map<String, Object> request) {
        try {
            Long adminId = ((Number) request.get("adminId")).longValue();
            String email = (String) request.get("email");
            String roleString = (String) request.get("role");

            // Validate invitation request
            accountCredentialService.validateInvitationRequest(email, roleString);

            // Validate and convert role
            UserProfile.Role role = accountCredentialService.validateRole(roleString);

            // Create and send invitation (business logic in service)
            var invitation = invitationService.createAndSendInvitation(adminId, email, role);

            // Build response
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "Invitation created successfully");
            response.put("token", invitation.getToken());
            response.put("email", invitation.getEmail());
            response.put("role", invitation.getRole().toString());
            response.put("expiresAt", invitation.getExpiresAt());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create invitation: " + e.getMessage()));
        }
    }

    /**
     * Register user with invitation token
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, Object> request) {
        try {
            String token = (String) request.get("token");
            String firstName = (String) request.get("firstName");
            String lastName = (String) request.get("lastName");
            String username = (String) request.get("username");
            String password = (String) request.get("password");
            String gender = (String) request.get("gender");
            String birthday = (String) request.get("birthday");
            String contactNumber = (String) request.get("contactNumber");

            // Accept invitation and create account (business logic in service)
            var newUser = invitationService.acceptInvitation(
                    token, firstName, lastName, username, password, gender, birthday, contactNumber
            );

            // Build response
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("id", newUser.getId());
            response.put("message", "Account created successfully");
            response.put("username", newUser.getUsername());
            response.put("role", newUser.getRole().toString());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to register user: " + e.getMessage()));
        }
    }
}
