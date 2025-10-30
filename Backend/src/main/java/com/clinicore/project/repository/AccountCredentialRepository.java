package com.clinicore.project.repository;

import com.clinicore.project.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountCredentialRepository extends JpaRepository<UserProfile, Long> {
    UserProfile findByUsernameAndPasswordHash(String username, String passwordHash);

    UserProfile findByUsername(String username);
}
