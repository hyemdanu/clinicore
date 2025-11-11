/**
 * Minh Nguyen
 *
 * Controller for users log-in and new user account registration
 * It uses the account credential service layer to authenticate users and create new user accounts
 *
 * Functions/Purposes:
 * - Only admins can create new user accounts for residents, admins, and caregivers
 */

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

    // service layers to be injected
    private final AccountCredentialService accountCredentialService;
    private final InvitationService invitationService;

    // inject service layers (service layers hold business logic - controller just forwards requests to service layers)
    public AccountCredentialController(AccountCredentialService accountCredentialService,
                                      InvitationService invitationService) {
        this.accountCredentialService = accountCredentialService;
        this.invitationService = invitationService;
    }

    // login endpoint
    // this will grab login details from the frontend and call the account credential service layer to authenticate the user
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserProfile loginDetails) {

        // this will call the account credential service layer to authenticate the user
        try {
            Map<String, Object> response = accountCredentialService.authenticateUser(

                    // we pass the username and password to the service layer to authenticate the user
                    loginDetails.getUsername(),
                    loginDetails.getPasswordHash()
            );

            // if the user is authenticated, we return OK response with the user profile data
            return ResponseEntity.ok(response);


        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Login failed: " + e.getMessage()));
        }
    }

    // endpoint for admins to create and send user registration invitations
    // this will grab request details from the frontend and call the account credential service layer to create and send the invitation
    @PostMapping("/invite")
    public ResponseEntity<?> createInvitation(@RequestBody Map<String, Object> request) {
        try {

            // grab body requests and store into variables
            Long adminId = ((Number) request.get("adminId")).longValue();
            String email = (String) request.get("email");
            String roleString = (String) request.get("role");

            // validate email and role is provided
            accountCredentialService.validateInvitationRequest(email, roleString);

            // validate role (service layer will validate role exists)
            // if validated, it will save the role as a role enum type from userProfile entity
            UserProfile.Role role = accountCredentialService.validateRole(roleString);

            // create and send invitation (business logic in service)
            // need to pass adminId of whos sending the invitation + email and role of invited user
            // this returns an invitation object with token, email, role, and expiration date
            var invitation = invitationService.createAndSendInvitation(adminId, email, role);

            // on invitation send success, build response from invitation object
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "Invitation created successfully");
            response.put("token", invitation.getToken());
            response.put("email", invitation.getEmail());
            response.put("role", invitation.getRole().toString());
            response.put("expiresAt", invitation.getExpiresAt());

            // return invitation object response
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create invitation: " + e.getMessage()));
        }
    }

    // endpoint to register new users
    // it will grab user details from the frontend and call the account credential service layer to create and save the user account
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, Object> request) { // request contains user details
        try {

            // save request details into variables
            String token = (String) request.get("token");
            String firstName = (String) request.get("firstName");
            String lastName = (String) request.get("lastName");
            String username = (String) request.get("username");
            String password = (String) request.get("password");
            String gender = (String) request.get("gender");
            String birthday = (String) request.get("birthday");
            String contactNumber = (String) request.get("contactNumber");
            String email = (String) request.get("email");

            // create account (business logic in invitation service layer) so send details to that
            // will return a new user object with all details filled in (new user account saved in db)
            var newUser = invitationService.acceptInvitation(
                    token, firstName, lastName, username, password, gender, birthday, contactNumber
            );

            // on success, build response from new user object
            // to display in frontend...
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

    @PostMapping("/forgot-userid")
    public ResponseEntity<?> forgotUserId(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            System.out.println("🔎 Received forgot-userid request for: " + email);  //

            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required."));
            }

            boolean exists = accountCredentialService.checkIfUserExistsByEmail(email);
            System.out.println("✅ Email exists? " + exists);  //

            if (exists) {
                return ResponseEntity.ok(Map.of("message", "Email verified successfully."));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Email not found in system."));
            }

        } catch (Exception e) {
            e.printStackTrace();  // 👈 print full stack trace
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to verify email: " + e.getMessage()));
        }
    }


    @PostMapping("/forgot-userid")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");

            if (email == null || email.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Email is required."));
            }

            boolean exists = accountCredentialService.checkIfUserExistsByEmail(email);

            if (exists) {
                //  return 200 OK when email exists
                return ResponseEntity.ok(Map.of("message", "Email verified successfully."));
            } else {
                //  return 404 if email not found
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Email not found in system."));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to verify email: " + e.getMessage()));
        }
    }
}
