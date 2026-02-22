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

    // if admin/caregiver/resident is sending the message
    @Enumerated(EnumType.STRING)
    @Column(name = "sender_role")
    private UserRole senderRole;

    @Column(name = "recipient_id")
    private Long recipientId;

    // if admin/caregiver/resident is receiving the message
    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_role")
    private UserRole recipientRole;

    // keeping subject for backwards compatibility, not used in chat UI
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(updatable = false, name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "is_read")
    private Boolean isRead = false;

    // get type of message from the enum
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType messageType = MessageType.TEXT;

    // conversation ID
    @Column(name = "conversation_id")
    private String conversationId;

    // file/image attachment
    @Column(name = "attachment_url")
    private String attachmentUrl;

    // filenames
    @Column(name = "attachment_name")
    private String attachmentName;

    // read with timestamp
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
        if (messageType == null) {
            messageType = MessageType.TEXT;
        }
        if (conversationId == null && senderId != null && recipientId != null) {
            Long smaller = Math.min(senderId, recipientId);
            Long larger = Math.max(senderId, recipientId);
            conversationId = smaller + "_" + larger;
        }
    }

    public enum UserRole {
        ADMIN,
        CAREGIVER,
        RESIDENT
    }

    // msg enum
    public enum MessageType {
        TEXT,
        IMAGE,
        FILE
    }
}