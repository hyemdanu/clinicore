package com.clinicore.project.controller;

import com.clinicore.project.entity.UserProfile;
import com.clinicore.project.entity.Invitation;
import com.clinicore.project.repository.AccountCredentialRepository;
import com.clinicore.project.repository.InvitationRepository;
import com.clinicore.project.service.InvitationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/accountCredential")
public class AccountCredentialController {
    @Autowired
    private AccountCredentialRepository accountCredentialRepository;

    // invitation service is used to create and send invitations for account resgistration
    @Autowired
    private InvitationService invitationService;

    /**
     * Login endpoint
     * Returns user ID, username, and role
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserProfile loginDetails) {

        // find those login details in the database --> see if theres a match
        UserProfile userProfile = accountCredentialRepository.findByUsernameAndPasswordHash(
                loginDetails.getUsername(), 
                loginDetails.getPasswordHash());
        
        if (userProfile != null) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("username", userProfile.getUsername());
            response.put("id", userProfile.getId());
            response.put("role", userProfile.getRole().toString());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid Credentials");
    }

    /**
     * ONLY admins creates and sends invitation for user resgistration
     * Admins can invite: RESIDENT, CAREGIVER, or other ADMIN users
     * 
     * Request body (these the fields that are required to send an invitation request):
     * {
     *   "adminId": 1, -> admin ID of the admin creating the invitation
     *   "email": "newuser@example.com", -> email of the invited user
     *   "role": R/A/C -> role of the invited user
     * }
     */
    @PostMapping("/invite")
    public ResponseEntity<?> createInvitation(@RequestBody Map<String, Object> request) {

        try {

            // variables
            Long adminId = ((Number) request.get("adminId")).longValue();
            String email = (String) request.get("email");
            String roleString = (String) request.get("role");
            
            // validate email is provided
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Email is required"));
            }

            // validate invited role is provided
            if (roleString == null || roleString.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Role is required"));
            }

            // validate invited role type
            UserProfile.Role role; // make role variable to validate then store
            try {
                role = UserProfile.Role.valueOf(roleString.toUpperCase()); // convert string to enum type role for user profile

            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid role. Use RESIDENT, CAREGIVER, or ADMIN"));
            }
            
            // use InvitationService to create invitation
            // InvitationService has all the core logic for creating and sending invitations
            // this returns token, email, role, and expiration date
            Invitation invitation = invitationService.createAndSendInvitation(adminId, email, role);

            // if invitation was successfully sent, return response to frontend (inv sent or sum)
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
     * Register users
     * Users must provide a valid invitation token to create an account (token generated when invitation is created)
     * We need token so no unwanted rando creations
     *
     * Request body (these the fields that are required to register a user; user fills this up):
     * {
     *   "token": "token from invitation",
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "username": "johndoe",
     *   "password": "securepassword",
     *   "gender": "Male",
     *   "birthday": "1990-01-15",
     *   "contactNumber": "555-1234"
     * }
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
            
            // validate required fields
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invitation token is required"));
            }
            
            // use InvitationService to accept invitation and create account
            // InvitationService has all the core logic for creating and saving new account users in db
            // this method returns the new user information (new user is saved in newUer variable)
            UserProfile newUser = invitationService.acceptInvitation(
                    token,
                    firstName,
                    lastName,
                    username,
                    password,
                    gender,
                    birthday,
                    contactNumber
            );

            // if user was created successfully, return response to frontend (account created or sum)
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
