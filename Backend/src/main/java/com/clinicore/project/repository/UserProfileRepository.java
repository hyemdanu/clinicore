package com.clinicore.project.repository;

import com.clinicore.project.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    
    /**
     * Find user by username
     * Used for login and checking if username exists
     */
    Optional<UserProfile> findByUsername(String username);
    
    /**
     * Find all users with a specific role
     * Used for filtering users by role (ADMIN, CAREGIVER, RESIDENT)
     */
    List<UserProfile> findByRole(UserProfile.Role role);
    
    /**
     * Count users with a specific role
     * Used for statistics
     */
    long countByRole(UserProfile.Role role);
}