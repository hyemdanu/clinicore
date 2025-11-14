package com.clinicore.project.service;

import com.clinicore.project.entity.AccountCreationRequest;
import com.clinicore.project.entity.UserProfile;
import com.clinicore.project.repository.AccountCreationRequestRepository;
import com.clinicore.project.repository.UserProfileRepository;
import com.clinicore.project.service.AccountRequestResultType;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AccountCreationRequestService {

    private final AccountCreationRequestRepository accountCreationRequestRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder; // likely used later for activation logic

    public AccountCreationRequestService(AccountCreationRequestRepository accountCreationRequestRepository,
                                         UserProfileRepository userProfileRepository,
                                         PasswordEncoder passwordEncoder) {
        this.accountCreationRequestRepository = accountCreationRequestRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }


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

        // no existing request -> create new request
        if (existACR.isEmpty()) {
            createNewRequest(firstName, lastName, email, role, now);
            return AccountRequestResultType.NEW;
        }

        // There IS an existing request with the given email
        AccountCreationRequest existing = existACR.get();

        // get stats of request
        String status = existing.getStatus(); // PENDING, APPROVED, COMPLETED, EXPIRED, DENIED

        // check if request has expired
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

        // PENDING -> user can still change role, name with that email since it hasnt been approved yet
        // basically send new request with that email
        if ("PENDING".equals(status) && !isExpiredByTime) {
            existing.setFirstName(firstName);
            existing.setLastName(lastName);
            existing.setRole(role);

            accountCreationRequestRepository.save(existing);
            return AccountRequestResultType.PENDING;
        }

        // EXPIRED OR DENIED --> send in a new request w same email
        if ("EXPIRED".equals(status) || "DENIED".equals(status) || isExpiredByTime) {
            existing.setFirstName(firstName);
            existing.setLastName(lastName);
            existing.setRole(role);
            existing.setStatus("PENDING");
            existing.setExpiresAt(now.plusDays(3));
            existing.setActivationAttempts(0);
            accountCreationRequestRepository.save(existing);

            return AccountRequestResultType.REOPEN;
        }

        // Fallback (should rarely hit if statuses are consistent)
        return AccountRequestResultType.NEW;
    }

    private void createNewRequest(String firstName,
                                  String lastName,
                                  String email,
                                  String role,
                                  LocalDateTime now) {


        AccountCreationRequest acr = new AccountCreationRequest();
        acr.setFirstName(firstName);
        acr.setLastName(lastName);
        acr.setEmail(email);
        acr.setRole(role);
        acr.setStatus("PENDING");
        acr.setCreatedAt(now);
        acr.setApprovedAt(null);
        acr.setApprovedByAdminId(null);
        acr.setExpiresAt(now.plusDays(3)); // 3-day window for admin to act
        acr.setActivationCodeHash(null);   // will be set upon approval
        acr.setActivationAttempts(0);

        accountCreationRequestRepository.save(acr);
    }

    // Keeping this for later when you implement "approve" / activation logic
    private String generateActivationCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        return sb.toString();
    }
}
