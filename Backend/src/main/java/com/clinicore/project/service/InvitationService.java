/**
 * Olei Amelie Ngan
 *
 * Service layer for new user signup invites
 * This service layer uses the invitation repository to create and accept invitations
 *
 * Functions/Purposes:
 * - Create and send invitations
 * - Accept invitation to create user account, and save new user in database
 */
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

    // variables
    private final InvitationRepository invitationRepository;
    private final UserProfileRepository userProfileRepository;

    // constructor that injects all repositories so the service can access user and invitation data
    public InvitationService(
            InvitationRepository invitationRepository,
            UserProfileRepository userProfileRepository) {
        this.invitationRepository = invitationRepository;
        this.userProfileRepository = userProfileRepository;
    }

    // admin creates invitation, invoked from account credential controller
    // needs to receieve admin sending invite, email and role of invited user
    @Transactional
    public Invitation createAndSendInvitation(Long adminId, String email, UserProfile.Role role) {
        
        // verify admin id exists and has admin
        UserProfile admin = userProfileRepository.findById(adminId)
                .filter(user -> user.getRole() == UserProfile.Role.ADMIN) // is they admin?
                .orElseThrow(() -> new IllegalArgumentException("Only admins can create invitations")); // if not, gtfo

        // create invitation (token auto-generated in db (check entity))
        Invitation invitation = new Invitation();
        invitation.setEmail(email);
        invitation.setRole(role);
        invitation.setStatus(Invitation.Status.PENDING);
        invitation.setCreatedByAdmin(admin);  // admin id who created invitation
        invitation.setCreatedByAdminName(admin.getFirstName() + " " + admin.getLastName());  // admin name who created invitation

        // save invitation to db
        // will need to set-up email invitations in the future...
        Invitation savedInvitation = invitationRepository.save(invitation);

        return savedInvitation;
    }

    // user accepts invitation, invoked from account credential controller
    // when users clicks on invitation link in email (register endpoint in the account credential controller)
    // needs to receieve param information (will need to integrate in frontend later...)
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
        
        // find invitation by token
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invitation token"));


        // Create new user for that token
        UserProfile newUser = new UserProfile();
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setUsername(username);
        newUser.setPasswordHash(password);
        newUser.setGender(gender);
        newUser.setBirthday(LocalDate.parse(birthday));
        newUser.setContactNumber(contactNumber);
        newUser.setEmail(invitation.getEmail());
        newUser.setRole(invitation.getRole());

        // save new user to db
        UserProfile savedUser = userProfileRepository.save(newUser);

        // mark invitation as accepted
        invitation.setStatus(Invitation.Status.ACCEPTED);
        invitation.setAcceptedAt(LocalDateTime.now());
        invitationRepository.save(invitation);

        // return new user profile
        return savedUser;
    }

    /**
     * Get all invitations created by a specific admin
     */
    public java.util.List<Invitation> getInvitationsByAdmin(Long adminId) {
        return invitationRepository.findByCreatedByAdminId(adminId);
    }


}