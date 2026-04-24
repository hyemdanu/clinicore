package com.clinicore.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("JpaDataSourceORMInspection")
@Table(name = "communication_portal", indexes = {
        @Index(name = "idx_conversation_id", columnList = "conversation_id"),
        @Index(name = "idx_conv_recipient_read", columnList = "conversation_id, recipient_id, is_read"),
        @Index(name = "idx_sender_id", columnList = "sender_id"),
        @Index(name = "idx_recipient_id", columnList = "recipient_id")
})
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

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType messageType = MessageType.TEXT;

    @Column(name = "conversation_id")
    private String conversationId;

    @Column(name = "attachment_name")
    private String attachmentName;

    @Column(name = "attachment_type")
    private String attachmentType;

    @Lob
    @Column(name = "attachment_data", columnDefinition = "LONGBLOB")
    private byte[] attachmentData;

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
        if (isRead == null) {
            isRead = false;
        }
        if (messageType == null) {
            messageType = MessageType.TEXT;
        }
        if (conversationId == null && senderId != null && recipientId != null) {
            long smaller = Math.min(senderId, recipientId);
            long larger = Math.max(senderId, recipientId);
            conversationId = smaller + "_" + larger;
        }
    }

    public enum UserRole {
        ADMIN,
        CAREGIVER,
        RESIDENT
    }

    public enum MessageType {
        TEXT,
        IMAGE,
        FILE
    }
}