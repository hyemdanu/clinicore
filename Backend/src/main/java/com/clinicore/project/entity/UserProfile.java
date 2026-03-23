package com.clinicore.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_profile", indexes = {
    @Index(name = "idx_user_profile_role", columnList = "role"),
    @Index(name = "idx_user_profile_password_reset_token", columnList = "password_reset_token")
})
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "email", unique = true, nullable = false)
    private String email;



    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private LocalDate birthday;

    @Column(name = "contact_number", nullable = false)
    private String contactNumber;
    
    @Column(unique = true, nullable = false)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_token_expires_at")
    private LocalDateTime passwordResetTokenExpiresAt;

    // Note: inverse @OneToOne mappings to Admin/Caregiver/Resident were removed.
    // Hibernate cannot lazily load inverse @OneToOne — it fires a SELECT per UserProfile
    // per mapping to check if the record exists. With 3 mappings × N users = 3N extra queries.
    // Instead, load Admin/Caregiver/Resident directly from their repositories when needed.

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Role {
        ADMIN,
        CAREGIVER,
        RESIDENT
    }


}