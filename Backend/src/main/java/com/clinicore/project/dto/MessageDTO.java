package com.clinicore.project.dto;

import com.clinicore.project.entity.CommunicationPortal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// DTO for chat messages
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {

    private Long id;
    private Long senderId;
    private String senderRole;
    private String senderName;
    private Long recipientId;
    private String recipientRole;
    private String recipientName;
    private String message;
    private String messageType;
    private String conversationId;
    private String attachmentUrl;
    private String attachmentName;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private Boolean isRead;

    // dto for messages
    public static MessageDTO fromEntity(CommunicationPortal msg) {
        if (msg == null) {
            return null;
        }

        MessageDTO dto = new MessageDTO();
        dto.setId(msg.getId());
        dto.setSenderId(msg.getSenderId());
        dto.setSenderRole(msg.getSenderRole() != null ? msg.getSenderRole().name() : null);
        dto.setRecipientId(msg.getRecipientId());
        dto.setRecipientRole(msg.getRecipientRole() != null ? msg.getRecipientRole().name() : null);
        dto.setMessage(msg.getMessage());
        dto.setMessageType(msg.getMessageType() != null ? msg.getMessageType().name() : "TEXT");
        dto.setConversationId(msg.getConversationId());
        // attachments are streamed from /api/messages/chat/attachment/{id}
        if (msg.getAttachmentName() != null && msg.getId() != null) {
            dto.setAttachmentUrl("/api/messages/chat/attachment/" + msg.getId());
        }
        dto.setAttachmentName(msg.getAttachmentName());
        dto.setSentAt(msg.getSentAt());
        dto.setReadAt(msg.getReadAt());
        dto.setIsRead(msg.getIsRead());

        return dto;
    }

    // convert entity to DTO with sender/recipient names
    public static MessageDTO fromEntityWithNames(CommunicationPortal msg, String senderName, String recipientName) {
        MessageDTO dto = fromEntity(msg);
        if (dto != null) {
            dto.setSenderName(senderName);
            dto.setRecipientName(recipientName);
        }
        return dto;
    }

    // overload: use provided plaintext instead of entity's (encrypted) message field
    public static MessageDTO fromEntityWithNames(CommunicationPortal msg, String senderName, String recipientName, String plaintextMessage) {
        MessageDTO dto = fromEntityWithNames(msg, senderName, recipientName);
        if (dto != null) {
            dto.setMessage(plaintextMessage);
        }
        return dto;
    }
}
