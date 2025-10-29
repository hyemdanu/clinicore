package com.clinicore.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "communication_portal")
public class CommunicationPortal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sender_id;
    
    @Enumerated(EnumType.STRING)
    private UserRole sender_role;
    
    private Long recipient_id;
    
    @Enumerated(EnumType.STRING)
    private UserRole recipient_role;
    
    private String subject;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    @Column(updatable = false)
    private LocalDateTime sent_at;
    
    private LocalDateTime read_at;
    private Boolean is_read = false;

    public void markAsRead() {
        this.is_read = true;
        this.read_at = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        sent_at = LocalDateTime.now();
        if (is_read == null) {
            is_read = false;
        }
    }

    public enum UserRole {
        ADMIN,
        CAREGIVER,
        RESIDENT
    }
}