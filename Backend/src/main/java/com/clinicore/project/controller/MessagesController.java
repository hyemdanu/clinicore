package com.clinicore.project.controller;

import com.clinicore.project.dto.ConversationDTO;
import com.clinicore.project.dto.MessageDTO;
import com.clinicore.project.entity.UserProfile;
import com.clinicore.project.repository.MessagesRepository;
import com.clinicore.project.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(value = "/chat/send-with-attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> sendMessageWithAttachment(
            @RequestParam Long senderId,
            @RequestParam Long recipientId,
            @RequestParam(required = false) String message,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is required"));
            }

            MessageDTO sent = messageService.sendMessageWithAttachment(
                    senderId,
                    recipientId,
                    message,
                    file.getBytes(),
                    file.getOriginalFilename(),
                    file.getContentType()
            );
            return ResponseEntity.ok(sent);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to send message: " + e.getMessage()));
        }
    }

    @GetMapping("/chat/attachment/{messageId}")
    public ResponseEntity<byte[]> getAttachment(@PathVariable Long messageId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getName() == null) {
                return ResponseEntity.status(401).build();
            }
            Long userId = Long.parseLong(auth.getName());

            MessagesRepository.AttachmentView view = messageService.getAttachment(messageId, userId);
            if (view == null) {
                return ResponseEntity.notFound().build();
            }

            String contentType = view.getAttachmentType() != null
                    ? view.getAttachmentType()
                    : MediaType.APPLICATION_OCTET_STREAM_VALUE;

            String filename = view.getAttachmentName() != null ? view.getAttachmentName() : "attachment";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(view.getAttachmentData());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
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