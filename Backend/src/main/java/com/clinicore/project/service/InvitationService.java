package com.clinicore.project.service;

import com.clinicore.project.entity.Invitation;
import com.clinicore.project.entity.UserProfile;
import com.clinicore.project.repository.InvitationRepository;
import com.clinicore.project.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final UserProfileRepository userProfileRepository;

    public InvitationService(
            InvitationRepository invitationRepository,
            UserProfileRepository userProfileRepository) {
        this.invitationRepository = invitationRepository;
        this.userProfileRepository = userProfileRepository;
    }

    /**
     * Admin creates invitation
     * TODO: Email will be sent here in production
     */
    @Transactional
    public Invitation createAndSendInvitation(
            Long adminId,
            String email,
            UserProfile.Role role) {
        
        // Verify admin exists and has ADMIN role
        UserProfile admin = userProfileRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));
        
        if (admin.getRole() != UserProfile.Role.ADMIN) {
            throw new IllegalArgumentException("Only admins can create invitations");
        }

        // Check if email already has account
        if (userProfileRepository.findByUsername(email).isPresent()) {
            throw new IllegalArgumentException("Email already has an account");
        }

        // Check if invitation already pending
        invitationRepository.findByEmail(email).ifPresent(invite -> {
            if (invite.getStatus() == Invitation.Status.PENDING && !invite.isExpired()) {
                throw new IllegalArgumentException("Invitation already sent to this email");
            }
        });

        // Create invitation
        Invitation invitation = new Invitation();
        invitation.setEmail(email);
        invitation.setRole(role);
        invitation.setStatus(Invitation.Status.PENDING);
        invitation.setCreatedByAdmin(admin);  // ← Sets the relationship (stores admin ID)
        invitation.setCreatedByAdminName(admin.getFirstName() + " " + admin.getLastName());  // ← Sets the name
        
        Invitation savedInvitation = invitationRepository.save(invitation);

        // TODO: Send email here in production
        System.out.println("📧 [TEST MODE] Invitation created for: " + email);
        System.out.println("   Token: " + savedInvitation.getToken());
        System.out.println("   Role: " + role);
        System.out.println("   Acceptance link: /accept-invitation/" + savedInvitation.getToken());

        return savedInvitation;
    }

    /**
     * User accepts invitation and creates account
     */
    @Transactional
    public UserProfile acceptInvitation(
            String token,
            String firstName,
            String lastName,
            String username,
            String password,
            String gender,
            String birthday,
            String contactNumber) {
        
        // Find invitation by token
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invitation token"));

        // Check if invitation is expired
        if (invitation.isExpired()) {
            invitation.setStatus(Invitation.Status.EXPIRED);
            invitationRepository.save(invitation);
            throw new IllegalArgumentException("Invitation has expired");
        }

        // Check if already accepted
        if (invitation.getStatus() == Invitation.Status.ACCEPTED) {
            throw new IllegalArgumentException("Invitation already used");
        }

        // Create new user
        UserProfile newUser = new UserProfile();
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setUsername(username);
        newUser.setPasswordHash(password);  //TODO: Use BCrypt in production
        newUser.setGender(gender);
        newUser.setBirthday(LocalDate.parse(birthday));
        newUser.setContactNumber(contactNumber);
        newUser.setRole(invitation.getRole());

        UserProfile savedUser = userProfileRepository.save(newUser);

        // Mark invitation as accepted
        invitation.setStatus(Invitation.Status.ACCEPTED);
        invitation.setAcceptedAt(LocalDateTime.now());
        invitationRepository.save(invitation);

        System.out.println("✅ Account created for: " + username + " with role: " + invitation.getRole());

        return savedUser;
    }

    /**
     * Get all invitations created by a specific admin
     */
    public java.util.List<Invitation> getInvitationsByAdmin(Long adminId) {
        return invitationRepository.findByCreatedByAdminId(adminId);
    }

    /**
     * Check if invitation exists and is valid
     */
    public boolean isInvitationValid(String token) {
        return invitationRepository.findByToken(token)
                .map(invitation -> !invitation.isExpired() && 
                     invitation.getStatus() == Invitation.Status.PENDING)
                .orElse(false);
    }
}