package com.clinicore.project.repository;

import com.clinicore.project.entity.AccountCreationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface AccountCreationRequestRepository extends JpaRepository<AccountCreationRequest, Long> {

    // Find by email (for checking duplicates or retrieving a request)
    Optional<AccountCreationRequest> findByEmail(String email);

    // Check if a request already exists for the email
    boolean existsByEmail(String email);

    // Filter by status
    Optional<AccountCreationRequest> findByEmailAndStatus(String email, String status);

    // Find all by status
    List<AccountCreationRequest> findByStatus(String status);

    // Find all ordered by creation date (newest first)
    @Query("SELECT acr FROM AccountCreationRequest acr ORDER BY acr.createdAt DESC")
    List<AccountCreationRequest> findAllOrderedByCreatedAtDesc();
}
