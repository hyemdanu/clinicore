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

    @Column(name = "sender_id")
    private Long senderId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "sender_role")
    private UserRole senderRole;
    
    @Column(name = "recipient_id")
    private Long recipientId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_role")
    private UserRole recipientRole;
    
    private String subject;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    @Column(updatable = false, name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Column(name = "is_read")
    private Boolean isRead = false;

    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
        if (isRead == null) {
            isRead = false;
        }
    }

    public enum UserRole {
        ADMIN,
        CAREGIVER,
        RESIDENT
    }
}