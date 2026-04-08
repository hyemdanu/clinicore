package com.clinicore.project.controller;

import com.clinicore.project.dto.ConversationDTO;
import com.clinicore.project.dto.MessageDTO;
import com.clinicore.project.entity.CommunicationPortal;
import com.clinicore.project.entity.UserProfile;
import com.clinicore.project.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class MessagesController {

    @Autowired
    private MessageService messageService;

    // all conversations for a user, latest on top
    @GetMapping("/chat/conversations")
    public ResponseEntity<?> getUserConversations(@RequestParam Long userId) {
        try {
            List<ConversationDTO> conversations = messageService.getUserConversations(userId);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to load conversations: " + e.getMessage()));
        }
    }

    @GetMapping("/chat/conversation/{conversationId}")
    public ResponseEntity<?> getConversationMessages(@PathVariable String conversationId) {
        try {
            List<MessageDTO> messages = messageService.getConversationMessages(conversationId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to load messages: " + e.getMessage()));
        }
    }

    @PostMapping("/chat/send")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> request) {
        try {
            Long senderId = Long.valueOf(request.get("senderId").toString());
            Long recipientId = Long.valueOf(request.get("recipientId").toString());
            String messageText = (String) request.get("message");

            if (messageText == null || messageText.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Message cannot be empty"));
            }
            if (messageText.length() > 5000) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Message must be under 5000 characters"));
            }

            MessageDTO sent = messageService.sendMessage(senderId, recipientId, messageText);
            return ResponseEntity.ok(sent);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to send message: " + e.getMessage()));
        }
    }

    @PostMapping("/chat/send-with-attachment")
    public ResponseEntity<?> sendMessageWithAttachment(@RequestBody Map<String, Object> request) {
        try {
            Long senderId = Long.valueOf(request.get("senderId").toString());
            Long recipientId = Long.valueOf(request.get("recipientId").toString());
            String messageText = (String) request.get("message");
            String messageTypeStr = (String) request.get("messageType");
            String attachmentUrl = (String) request.get("attachmentUrl");
            String attachmentName = (String) request.get("attachmentName");

            CommunicationPortal.MessageType messageType = CommunicationPortal.MessageType.FILE;
            if (messageTypeStr != null) {
                messageType = CommunicationPortal.MessageType.valueOf(messageTypeStr.toUpperCase());
            }

            MessageDTO sent = messageService.sendMessageWithAttachment(
                    senderId, recipientId, messageText, messageType, attachmentUrl, attachmentName
            );
            return ResponseEntity.ok(sent);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to send message: " + e.getMessage()));
        }
    }

    @PatchMapping("/chat/conversation/{conversationId}/read")
    public ResponseEntity<?> markConversationAsRead(@PathVariable String conversationId,
                                                     @RequestParam Long userId) {
        try {
            messageService.markConversationAsRead(conversationId, userId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to mark as read: " + e.getMessage()));
        }
    }

    @GetMapping("/chat/unread-count")
    public ResponseEntity<?> getUnreadCount(@RequestParam Long userId) {
        try {
            Integer count = messageService.getTotalUnreadCount(userId);
            return ResponseEntity.ok(Map.of("unreadCount", count));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get unread count: " + e.getMessage()));
        }
    }

    @GetMapping("/chat/available-users")
    public ResponseEntity<?> getAvailableUsers(@RequestParam Long currentUserId) {
        try {
            List<UserProfile> users = messageService.getAvailableUsers(currentUserId);

            List<Map<String, Object>> userList = users.stream()
                    .map(user -> Map.of(
                            "id", (Object) user.getId(),
                            "name", user.getFirstName() + " " + user.getLastName(),
                            "role", user.getRole() != null ? user.getRole().name() : "UNKNOWN"
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(userList);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get users: " + e.getMessage()));
        }
    }

    @GetMapping("/chat/start/{otherUserId}")
    public ResponseEntity<?> startConversation(@PathVariable Long otherUserId,
                                                @RequestParam Long currentUserId) {
        try {
            String conversationId = messageService.generateConversationId(currentUserId, otherUserId);
            return ResponseEntity.ok(Map.of(
                    "conversationId", conversationId,
                    "currentUserId", currentUserId,
                    "otherUserId", otherUserId
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to start conversation: " + e.getMessage()));
        }
    }
}