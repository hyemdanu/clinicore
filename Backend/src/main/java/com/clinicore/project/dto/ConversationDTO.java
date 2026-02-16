package com.clinicore.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// DTO for conversation list
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {

    private String conversationId;

    // other users convo
    private Long otherUserId;
    private String otherUserName;
    private String otherUserRole;

    // last message preview
    private String lastMessage;
    private String lastMessageType;
    private LocalDateTime lastMessageAt;
    private Long lastMessageSenderId;

    // how many unread messages in this conversation
    private Integer unreadCount;
}
