package com.clinicore.project.service;

import java.util.List;

import com.clinicore.project.entity.AccountCreationRequest;

import com.clinicore.project.entity.UserProfile;
import com.clinicore.project.entity.Admin;
import com.clinicore.project.entity.Caregiver;
import com.clinicore.project.entity.Resident;
import com.clinicore.project.repository.AccountCreationRequestRepository;
import com.clinicore.project.repository.AdminRepository;
import com.clinicore.project.repository.CaregiverRepository;
import com.clinicore.project.repository.ResidentGeneralRepository;
import com.clinicore.project.repository.UserProfileRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.LinkedHashMap;

@Service
public class AccountCreationRequestService {

    private final AccountCreationRequestRepository accountCreationRequestRepository;

    private final UserProfileRepository userProfileRepository;
    private final ResidentGeneralRepository residentRepository;
    private final CaregiverRepository caregiverRepository;
    private final AdminRepository adminRepository;

    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;


    // construct the service layer with all repositories
    public AccountCreationRequestService(
            AccountCreationRequestRepository accountCreationRequestRepository,

            UserProfileRepository userProfileRepository,
            ResidentGeneralRepository residentRepository,
            CaregiverRepository caregiverRepository,
            AdminRepository adminRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder) {
        this.accountCreationRequestRepository = accountCreationRequestRepository;

        this.userProfileRepository = userProfileRepository;
        this.residentRepository = residentRepository;
        this.caregiverRepository = caregiverRepository;
        this.adminRepository = adminRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }


    // return if requesting account already exists (if so, status)
    // returns (so UI knows display msg):
    // public enum AccountRequestResultType {
    //    USER_ALREADY_EXISTS,   // user already exists in db

    //    NEW,                   // no existing request -> create new pending request

    //    PENDING,               // existing PENDING -> user has pending request, user/admin request details can be changed still (e.g., name, role)
    //    APPROVED,              // request already APPROVED -> user must create account; only admins can change request details
    //    COMPLETED,             // user COMPLETED account creation -> user should exist in db
    //    REOPEN (for EXP/DEN requests),  // request EXPIRED or DENIED (we mark it so it goes away from UI after idle). if new request after expiry/denial -> reopen as PENDING
    //}
    // return type depends on status from account.creation.reqs db (PENDING, APPROVED, DENIED, COMPLETED, EXPIRED)
    public AccountRequestResultType createAccountRequest(String firstName,
                                                         String lastName,
                                                         String email,
                                                         String role) {

        // get current time
        LocalDateTime now = LocalDateTime.now();

        // If user already has a profile, do not let them request -> tell them to log in >:(
        Optional<UserProfile> exist = userProfileRepository.findByEmail(email);
        if (exist.isPresent()) {
            return AccountRequestResultType.USER_ALREADY_EXISTS;
        }


        // if user doesnt have a profile:

        // check if this email has alr requested
        Optional<AccountCreationRequest> existACR =
                accountCreationRequestRepository.findByEmail(email);

            // no existing request -> create new request in account.creation.request table
            // return NEW
            if (existACR.isEmpty()) {

                // call method to create new request
                createNewRequest(firstName, lastName, email, role, now);
                return AccountRequestResultType.NEW;
            }

        // If there IS an existing request with the given email, grab info
        AccountCreationRequest existing = existACR.get();

        // get stats of existing request
        String status = existing.getStatus(); // PENDING, APPROVED, COMPLETED, EXPIRED, DENIED

        // check if request were expired
        boolean isExpiredByTime = existing.getExpiresAt() != null &&  existing.getExpiresAt().isBefore(now);

        // COMPLETED -> account already created, tell user to log in
        if ("COMPLETED".equals(status)) {
            return AccountRequestResultType.COMPLETED;
        }

        // APPROVED -> request already approved, tell user to create account
        // any changes to user information from request, admin must do (e.g. name and role)
        if ("APPROVED".equals(status) && !isExpiredByTime) {
            return AccountRequestResultType.APPROVED;
        }

        // PENDING -> user can still change request details of that email since it hasnt been approved yet
        // basically send edit request details of that email
        if ("PENDING".equals(status) && !isExpiredByTime) {

            // set new details
            existing.setFirstName(firstName);
            existing.setLastName(lastName);
            existing.setRole(role);

            // save new details
            accountCreationRequestRepository.save(existing);
            return AccountRequestResultType.PENDING;
        }

        // EXPIRED OR DENIED --> send in a new request w same email
        if ("EXPIRED".equals(status) || "DENIED".equals(status) || isExpiredByTime) {
            existing.setFirstName(firstName);
            existing.setLastName(lastName);
            existing.setRole(role);
            existing.setStatus("PENDING");

            // extend expiration & reset activation attempts
            existing.setExpiresAt(now.plusDays(3));
            existing.setActivationAttempts(0);
            accountCreationRequestRepository.save(existing);

            return AccountRequestResultType.REOPEN;
        }

        // Fallback (should rarely hit if statuses are consistent)
        return AccountRequestResultType.NEW;
    }

    // method to create new request in db
    private void createNewRequest(String firstName,
                                  String lastName,
                                  String email,
                                  String role,
                                  LocalDateTime now) {

        // set fields
        AccountCreationRequest acr = new AccountCreationRequest();
        acr.setFirstName(firstName);
        acr.setLastName(lastName);
        acr.setEmail(email);
        acr.setRole(role);
        acr.setStatus("PENDING");
        acr.setCreatedAt(now);
        acr.setApprovedAt(null);
        acr.setApprovedByAdminId(null);
        acr.setExpiresAt(now.plusDays(5)); // 5 day window for admin to approve/deny
        acr.setActivationCodeHash(null);   // code will be set upon approval
        acr.setActivationAttempts(0);      // this so we can protect creation from bots

        // save in repo
        accountCreationRequestRepository.save(acr);
    }
















    // for admins
    /**
     * Get all account requests (for admin dashboard)
     */
    public List<AccountCreationRequest> getAllAccountRequests() {
        return accountCreationRequestRepository.findAll();
    }

    /**
     * Update account request fields (admin can edit)
     */
    public AccountCreationRequest updateAccountRequest(Long requestId, String firstName, String lastName, String email) {
        AccountCreationRequest request = accountCreationRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Account request not found"));

        // Only allow editing if status is PENDING or APPROVED
        if (!"PENDING".equals(request.getStatus()) && !"APPROVED".equals(request.getStatus())) {
            throw new IllegalArgumentException("Can only edit requests with PENDING or APPROVED status");
        }

        // Validate new email isn't already used if changed
        if (!request.getEmail().equals(email)) {
            if (userProfileRepository.findByEmail(email).isPresent()) {
                throw new IllegalArgumentException("Email already exists in system");
            }
            if (accountCreationRequestRepository.findByEmail(email).isPresent()) {
                throw new IllegalArgumentException("Email already has a pending request");
            }
        }

        if (firstName != null && !firstName.isBlank()) {
            request.setFirstName(firstName);
        }
        if (lastName != null && !lastName.isBlank()) {
            request.setLastName(lastName);
        }
        if (email != null && !email.isBlank()) {
            request.setEmail(email);
        }

        return accountCreationRequestRepository.save(request);
    }

    /**
     * Approve account request and generate activation code
     */
    public String approveAccountRequest(Long requestId, Long adminId) {
        AccountCreationRequest request = accountCreationRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Account request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalArgumentException("Only PENDING requests can be approved");
        }

        // Generate activation code
        String activationCode = generateActivationCode(8);
        String activationCodeHash = passwordEncoder.encode(activationCode);

        request.setStatus("APPROVED");
        request.setApprovedAt(LocalDateTime.now());
        request.setApprovedByAdminId(adminId);
        request.setActivationCodeHash(activationCodeHash);
        request.setExpiresAt(LocalDateTime.now().plusDays(7)); // 7 days to activate

        accountCreationRequestRepository.save(request);

        // send email
        emailService.sendActivationCode(request.getEmail(), activationCode);

        return activationCode; // Return plaintext to show to admin (for sharing with user)
    }

    /**
     * Deny account request
     */
    public void denyAccountRequest(Long requestId, String reason) {
        AccountCreationRequest request = accountCreationRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Account request not found"));

        if ("COMPLETED".equals(request.getStatus()) || "DENIED".equals(request.getStatus())) {
            throw new IllegalArgumentException("Cannot deny already " + request.getStatus() + " requests");
        }

        request.setStatus("DENIED");
        accountCreationRequestRepository.save(request);

    }

    /**
     * Resend activation code (only for APPROVED requests)
     */
    public String resendActivationCode(Long requestId) {
        AccountCreationRequest request = accountCreationRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Account request not found"));

        if (!"APPROVED".equals(request.getStatus())) {
            throw new IllegalArgumentException("Can only resend codes for APPROVED requests");
        }

        // Generate new activation code
        String activationCode = generateActivationCode(8);
        String activationCodeHash = passwordEncoder.encode(activationCode);

        request.setActivationCodeHash(activationCodeHash);
        request.setActivationAttempts(0); // Reset attempts
        request.setExpiresAt(LocalDateTime.now().plusDays(7)); // Extend expiration

        accountCreationRequestRepository.save(request);

        // resend email
        emailService.sendActivationCode(request.getEmail(), activationCode);

        return activationCode;
    }

    // Make generateActivationCode public so it can be used
    public String generateActivationCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        return sb.toString();

    }































    /**
     * Verify activation code with three possible outcomes:
     * 1. SUCCESS: Email + Code match → Return account details
     * 2. INVALID_CODE: Email exists but code doesn't match → Increment attempts
     * 3. NOT_FOUND: Email doesn't exist → No approved request found
     */
    @Transactional
    public Map<String, Object> verifyActivationCode(String email, String activationCode) {
        Map<String, Object> response = new LinkedHashMap<>();

        // Find account creation request by email
        AccountCreationRequest accountRequest = accountCreationRequestRepository
                .findByEmail(email)
                .orElse(null);

        // Email doesn't exist or not apprroved
        if (accountRequest == null || !"APPROVED".equals(accountRequest.getStatus())) {
            response.put("success", false);
            response.put("message", "No approved account request found for this email");
            response.put("status", "NOT_FOUND");
            return response;
        }

        // Check if request has expired
        if (accountRequest.getExpiresAt().isBefore(LocalDateTime.now())) {
            response.put("success", false);
            response.put("message", "Activation code has expired or invalid email.");
            response.put("status", "EXPIRED");
            return response;
        }

        // Check activation attempts (optional: block after too many attempts)
        if (accountRequest.getActivationAttempts() >= 5) {
            response.put("success", false);
            response.put("message", "Too many failed activation attempts. Please request a new code.");
            response.put("status", "BLOCKED");
            return response;
        }

        // Verify activation code matches
        boolean codeMatches = verifyActivationCodeHash(activationCode, accountRequest.getActivationCodeHash());

        //boolean codeMatches = true;
        if (!codeMatches) {
            accountRequest.setActivationAttempts(accountRequest.getActivationAttempts() + 1);
            accountCreationRequestRepository.save(accountRequest);

            response.put("success", false);
            response.put("message", "Invalid activation code");
            response.put("status", "INVALID_CODE");
            response.put("attemptsRemaining", 5 - accountRequest.getActivationAttempts());
            return response;
        }

        response.put("success", true);
        response.put("message", "Activation code verified");
        response.put("status", "VERIFIED");
        response.put("requestId", accountRequest.getId());
        response.put("firstName", accountRequest.getFirstName());
        response.put("lastName", accountRequest.getLastName());
        response.put("email", accountRequest.getEmail());
        response.put("role", accountRequest.getRole());

        return response;
    }


    private boolean verifyActivationCodeHash(String plainActivationCode, String storedHash) {
        return passwordEncoder.matches(plainActivationCode, storedHash);
    }

    @Transactional
    public UserProfile completeAccountActivation(
            Long requestId,
            String username,
            String password,
            String gender,
            String birthday,
            String contactNumber,
            Map<String, Object> roleSpecificData) {

        AccountCreationRequest request = accountCreationRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        if (!request.getStatus().equals("APPROVED")) {
            throw new IllegalArgumentException("Request not approved");
        }

        if (userProfileRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already taken");
        }

        // need to encode later
        String hashedPassword = password;

        UserProfile newUser = new UserProfile();
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setEmail(request.getEmail());
        newUser.setUsername(username);
        newUser.setPasswordHash(hashedPassword);
        newUser.setGender(gender);
        newUser.setBirthday(LocalDate.parse(birthday));
        newUser.setContactNumber(contactNumber);
        newUser.setRole(UserProfile.Role.valueOf(request.getRole()));

        UserProfile savedUser = userProfileRepository.save(newUser);

        String role = request.getRole();
        switch (role) {
            case "RESIDENT":
                createResidentProfile(savedUser, roleSpecificData);
                break;
            case "CAREGIVER":
                createCaregiverProfile(savedUser, roleSpecificData);
                break;
            case "ADMIN":
                createAdminProfile(savedUser);
                break;
        }

        request.setStatus("COMPLETED");
        accountCreationRequestRepository.save(request);

        // notify the new user
        String fullNameN = savedUser.getFirstName() + " " + savedUser.getLastName();
        String usernameN = savedUser.getUsername();
        String roleN = savedUser.getRole().toString();
        emailService.sendAccountCreatedConfirmation(savedUser.getEmail(), fullNameN, usernameN, roleN);

        // notify the admin who approved the request
        if (request.getApprovedByAdminId() != null) {
            userProfileRepository.findById(request.getApprovedByAdminId()).ifPresent(admin ->
                emailService.sendAccountCreatedConfirmation(admin.getEmail(), fullNameN, usernameN, roleN)
            );
        }

        return savedUser;
    }

    private void createResidentProfile(UserProfile userProfile, Map<String, Object> roleSpecificData) {
        Resident resident = new Resident();
        resident.setUserProfile(userProfile);
        resident.setEmergencyContactName((String) roleSpecificData.getOrDefault("emergencyContactName", ""));
        resident.setEmergencyContactNumber((String) roleSpecificData.getOrDefault("emergencyContactNumber", ""));
        resident.setNotes((String) roleSpecificData.getOrDefault("notes", ""));
        residentRepository.save(resident);
    }

    private void createCaregiverProfile(UserProfile userProfile, Map<String, Object> roleSpecificData) {
        Caregiver caregiver = new Caregiver();
        caregiver.setUserProfile(userProfile);
        caregiver.setNotes((String) roleSpecificData.getOrDefault("notes", ""));
        caregiverRepository.save(caregiver);
    }

    private void createAdminProfile(UserProfile userProfile) {
        Admin admin = new Admin();
        admin.setUserProfile(userProfile);
        adminRepository.save(admin);
    }
}
