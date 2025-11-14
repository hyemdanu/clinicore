package com.clinicore.project.repository;

import com.clinicore.project.entity.AccountCreationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountCreationRequestRepository extends JpaRepository<AccountCreationRequest, Long> {

    // Find by email (for checking duplicates or retrieving a request)
    Optional<AccountCreationRequest> findByEmail(String email);

    // Check if a request already exists for the email
    boolean existsByEmail(String email);

    // Filter by status
    Optional<AccountCreationRequest> findByEmailAndStatus(String email, String status);
}
