package com.clinicore.project.repository;

import com.clinicore.project.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    
    /**
     * Find invitation by unique token
     */
    Optional<Invitation> findByToken(String token);
    
    /**
     * Find invitation by email address
     */
    Optional<Invitation> findByEmail(String email);
    
    /**
     * Find all invitations created by a specific admin
     */
    List<Invitation> findByCreatedByAdminId(Long createdByAdminId);
}