package com.clinicore.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "invitation")
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String token;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserProfile.Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column
    private LocalDateTime acceptedAt;

    // name is preserved even if admin is deleted (they quit or sum)
    @Column(length = 255)
    private String createdByAdminName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_admin_id", insertable = false, updatable = false)
    private UserProfile createdByAdmin;

    public enum Status {
        PENDING,
        ACCEPTED,
        EXPIRED
    }

    // token is unique and generated on creation
    // this is needed to prevent duplicate invitations and also to prevent unauthorized access
    @PrePersist
    protected void onCreate() {
        if (this.token == null) {
            this.token = UUID.randomUUID().toString();
        }
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusDays(7);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}