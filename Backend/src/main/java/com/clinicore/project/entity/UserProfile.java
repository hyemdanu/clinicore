package com.clinicore.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_profile")
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



    // child entities (admin/caregiver/resident) own these relationships via @MapsId
    // Lazy loading is used to avoid fetching unnecessary data
    @OneToOne(mappedBy = "userProfile", fetch = FetchType.LAZY)
    private Admin admin;

    @OneToOne(mappedBy = "userProfile", fetch = FetchType.LAZY)
    private Caregiver caregiver;

    @OneToOne(mappedBy = "userProfile", fetch = FetchType.LAZY)
    private Resident resident;

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