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

import com.clinicore.project.entity.AccountCreationRequest;
import com.clinicore.project.entity.UserProfile;
import com.clinicore.project.service.AccountCredentialService;
import com.clinicore.project.service.AccountCreationRequestService;
import com.clinicore.project.service.AccountRequestResultType;
import com.clinicore.project.repository.UserProfileRepository;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/accountCredential")
public class AccountCredentialController {

    // service layers to be injected
    private final AccountCredentialService accountCredentialService;
    private final AccountCreationRequestService accountCreationRequestService;
    private final UserProfileRepository userProfileRepository;
    private final com.clinicore.project.service.EmailService emailService;

    // inject service layers
    public AccountCredentialController(AccountCredentialService accountCredentialService,
                                       AccountCreationRequestService accountCreationRequestService,
                                       UserProfileRepository userProfileRepository,
                                       com.clinicore.project.service.EmailService emailService) {
        this.accountCredentialService = accountCredentialService;
        this.accountCreationRequestService = accountCreationRequestService;
        this.userProfileRepository = userProfileRepository;
        this.emailService = emailService;
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


    // register a new user
    @PostMapping("/request-access")
    public ResponseEntity<?> requestAccess(@RequestBody Map<String, String> request) {
        try {

            // extract values from the data sent from the frontend
            String firstName = request.get("firstName");
            String lastName = request.get("lastName");
            String email = request.get("email");
            String role = request.get("role");

            // validate fields again
            if (role == null || role.isBlank()
               || firstName == null || firstName.isBlank()
               || lastName == null || lastName.isBlank()
               || email == null || email.isBlank()) {

                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Missing required fields"));
            }

            // since we have fields, go to service to create account request w fields
            AccountRequestResultType result =
                    accountCreationRequestService.createAccountRequest(firstName, lastName, email, role);

            String message = switch (result) {
                case USER_ALREADY_EXISTS -> "An account already exists for this email. Please log in.";
                case PENDING -> "Your request has been updated. Please wait for admin approval.";
                case NEW, REOPEN -> "Your request has been sent. Please wait for admin approval.";
                case APPROVED ->
                        "Your request's already approved. Please go to the activation page to complete your account.";

                // this should rarely happen, but if it does, then ya
                case COMPLETED -> "An account has already been created for this email.";
            };

            //{
            //  "message": "Your request is already approved. Please go to the activation page.",
            //  "status": "APPROVED"
            //}
            return ResponseEntity.ok(Map.of(
                    "message", message,
                    "status", result.name()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to process request"));
        }
    }























    // FOR ADMIN
    @GetMapping("/account-requests")
    public ResponseEntity<?> getAllAccountRequests(@RequestParam Long adminId) {
        try {
            // validate admin
            UserProfile admin = userProfileRepository.findById(adminId)
                    .filter(user -> user.getRole() == UserProfile.Role.ADMIN)
                    .orElseThrow(() -> new IllegalArgumentException("Only admins can view account requests"));

            List<AccountCreationRequest> requests = accountCreationRequestService.getAllAccountRequests();
            return ResponseEntity.ok(requests);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch account requests: " + e.getMessage()));
        }
    }

    @PutMapping("/account-requests/{requestId}")
    public ResponseEntity<?> updateAccountRequest(
            @PathVariable Long requestId,
            @RequestParam Long adminId,
            @RequestBody Map<String, String> updates) {
        try {
            // validate admin
            UserProfile admin = userProfileRepository.findById(adminId)
                    .filter(user -> user.getRole() == UserProfile.Role.ADMIN)
                    .orElseThrow(() -> new IllegalArgumentException("Only admins can update account requests"));

            String firstName = updates.get("firstName");
            String lastName = updates.get("lastName");
            String email = updates.get("email");

            AccountCreationRequest updatedRequest = accountCreationRequestService.updateAccountRequest(
                    requestId, firstName, lastName, email
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Account request updated successfully",
                    "request", updatedRequest
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update account request: " + e.getMessage()));
        }
    }

    @PostMapping("/account-requests/{requestId}/approve")
    public ResponseEntity<?> approveAccountRequest(
            
            @PathVariable Long requestId,
            @RequestParam Long adminId) {
        try {
            // validate admin
            UserProfile admin = userProfileRepository.findById(adminId)
                    .filter(user -> user.getRole() == UserProfile.Role.ADMIN)
                    .orElseThrow(() -> new IllegalArgumentException("Only admins can approve requests"));

            String activationCode = accountCreationRequestService.approveAccountRequest(requestId, adminId);

            return ResponseEntity.ok(Map.of(
                    "message", "Account request approved successfully",
                    "activationCode", activationCode
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to approve account request: " + e.getMessage()));
        }
    }

    @PostMapping("/account-requests/{requestId}/deny")
    public ResponseEntity<?> denyAccountRequest(
            @PathVariable Long requestId,
            @RequestParam Long adminId,
            @RequestParam(required = false) String reason) {
        try {
            // validate admin
            UserProfile admin = userProfileRepository.findById(adminId)
                    .filter(user -> user.getRole() == UserProfile.Role.ADMIN)
                    .orElseThrow(() -> new IllegalArgumentException("Only admins can deny requests"));

            accountCreationRequestService.denyAccountRequest(requestId, reason);

            return ResponseEntity.ok(Map.of(
                    "message", "Account request denied"
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to deny account request: " + e.getMessage()));
        }
    }

    @PostMapping("/account-requests/{requestId}/resend-activation-code")
    public ResponseEntity<?> resendActivationCode(
            @PathVariable Long requestId,
            @RequestParam Long adminId) {
        try {
            // validate admin
            UserProfile admin = userProfileRepository.findById(adminId)
                    .filter(user -> user.getRole() == UserProfile.Role.ADMIN)
                    .orElseThrow(() -> new IllegalArgumentException("Only admins can resend activation codes"));

            String newActivationCode = accountCreationRequestService.resendActivationCode(requestId);

            return ResponseEntity.ok(Map.of(
                    "message", "Activation code resent successfully",
                    "activationCode", newActivationCode
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to resend activation code: " + e.getMessage()));
        }
    }


















    // acccouint creation
    @PostMapping("/verify-activation-code")
    public ResponseEntity<?> verifyActivationCode(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String activationCode = request.get("activationCode");

            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email is required"));
            }
            if (activationCode == null || activationCode.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Activation code is required"));
            }

            Map<String, Object> verificationResult = accountCreationRequestService
                    .verifyActivationCode(email, activationCode);

            return ResponseEntity.ok(verificationResult);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Verification failed"));
        }
    }

    @PostMapping("/create-account")
    public ResponseEntity<?> completeAccountActivation(@RequestBody Map<String, Object> request) {
        try {
            Long requestId = ((Number) request.get("requestId")).longValue();
            String username = (String) request.get("username");
            String password = (String) request.get("password");
            String gender = (String) request.get("gender");
            String birthday = (String) request.get("birthday");
            String contactNumber = (String) request.get("contactNumber");
            String role = (String) request.get("role");

            if (username == null || username.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Username is required"));
            }
            if (password == null || password.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Password is required"));
            }
            if (gender == null || gender.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Gender is required"));
            }
            if (birthday == null || birthday.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Birthday is required"));
            }
            if (contactNumber == null || contactNumber.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Contact number is required"));
            }

            Map<String, Object> roleSpecificData = new LinkedHashMap<>();
            
            if ("RESIDENT".equals(role)) {
                String emergencyContactName = (String) request.get("emergencyContactName");
                String emergencyContactNumber = (String) request.get("emergencyContactNumber");
                
                if (emergencyContactName == null || emergencyContactName.isBlank()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Emergency contact name is required"));
                }
                if (emergencyContactNumber == null || emergencyContactNumber.isBlank()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Emergency contact number is required"));
                }
                
                roleSpecificData.put("emergencyContactName", emergencyContactName);
                roleSpecificData.put("emergencyContactNumber", emergencyContactNumber);
                roleSpecificData.put("notes", request.getOrDefault("notes", ""));
                
            } else if ("CAREGIVER".equals(role)) {
                roleSpecificData.put("notes", request.getOrDefault("notes", ""));
            }

            UserProfile newUser = accountCreationRequestService.completeAccountActivation(
                    requestId, username, password, gender, birthday, contactNumber, roleSpecificData
            );

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "Account created successfully");
            response.put("userId", newUser.getId());
            response.put("username", newUser.getUsername());
            response.put("role", newUser.getRole().toString());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create account"));
        }
    }

    @PostMapping("/forgot-userid")
    public ResponseEntity<?> forgotUserId(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");

            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required."));
            }

            String username = accountCredentialService.getUsernameByEmail(email);
            emailService.sendUsernameReminder(email, username);
            return ResponseEntity.ok(Map.of("message", "Username sent to your email."));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process request: " + e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");

            if (email == null || email.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Email is required."));
            }

            accountCredentialService.sendPasswordReset(email);
            return ResponseEntity.ok(Map.of("message", "Password reset email sent."));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process request: " + e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String newPassword = request.get("newPassword");

            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required."));
            }
            if (newPassword == null || newPassword.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "New password is required."));
            }

            accountCredentialService.resetPassword(email, newPassword);
            return ResponseEntity.ok(Map.of("message", "Password reset successfully."));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to reset password: " + e.getMessage()));
        }
    }
}
