package com.clinicore.project.controller;

import com.clinicore.project.dto.ConversationDTO;
import com.clinicore.project.dto.MessageDTO;
import com.clinicore.project.entity.CommunicationPortal;
import com.clinicore.project.entity.UserProfile;
import com.clinicore.project.repository.MessagesRepository;
import com.clinicore.project.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*") // Adjust based on frontend URL
public class MessagesController {

    @Autowired
    private MessagesRepository messagesRepository;

    @Autowired
    private MessageService messageService;

    /**
     * Get all messages
     */
    @GetMapping
    public ResponseEntity<List<CommunicationPortal>> getAllMessages() {
        try {
            List<CommunicationPortal> messages = messagesRepository.findAll();
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get message by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CommunicationPortal> getMessageById(@PathVariable Long id) {
        try {
            Optional<CommunicationPortal> message = messagesRepository.findById(id);
            return message.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create a new message
     */
    @PostMapping
    public ResponseEntity<CommunicationPortal> createMessage(@RequestBody CommunicationPortal message) {
        try {
            CommunicationPortal savedMessage = messagesRepository.save(message);
            return ResponseEntity.ok(savedMessage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update an existing message
     */
    @PutMapping("/{id}")
    public ResponseEntity<CommunicationPortal> updateMessage(@PathVariable Long id,
                                                             @RequestBody CommunicationPortal messageDetails) {
        try {
            Optional<CommunicationPortal> optionalMessage = messagesRepository.findById(id);
            if (optionalMessage.isPresent()) {
                CommunicationPortal message = optionalMessage.get();

                if (messageDetails.getMessage() != null) {
                    message.setMessage(messageDetails.getMessage());
                }
                if (messageDetails.getSubject() != null) {
                    message.setSubject(messageDetails.getSubject());
                }
                if (messageDetails.getIsRead() != null) {
                    message.setIsRead(messageDetails.getIsRead());
                    if (messageDetails.getIsRead()) {
                        message.setReadAt(LocalDateTime.now());
                    }
                }
                if (messageDetails.getSenderRole() != null) {
                    message.setSenderRole(messageDetails.getSenderRole());
                }
                if (messageDetails.getRecipientRole() != null) {
                    message.setRecipientRole(messageDetails.getRecipientRole());
                }

                CommunicationPortal updatedMessage = messagesRepository.save(message);
                return ResponseEntity.ok(updatedMessage);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete a message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        try {
            if (messagesRepository.existsById(id)) {
                messagesRepository.deleteById(id);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all messages by sender
     */
    @GetMapping("/sender/{senderId}")
    public ResponseEntity<List<CommunicationPortal>> getMessagesBySender(@PathVariable Long senderId) {
        try {
            List<CommunicationPortal> messages = messagesRepository.findBySenderId(senderId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all messages by recipient
     */
    @GetMapping("/recipient/{recipientId}")
    public ResponseEntity<List<CommunicationPortal>> getMessagesByRecipient(@PathVariable Long recipientId) {
        try {
            List<CommunicationPortal> messages = messagesRepository.findByRecipientId(recipientId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get messages by sender role
     */
    @GetMapping("/sender-role/{senderRole}")
    public ResponseEntity<List<CommunicationPortal>> getMessagesBySenderRole(@PathVariable CommunicationPortal.UserRole senderRole) {
        try {
            List<CommunicationPortal> messages = messagesRepository.findBySenderRole(senderRole);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get messages between sender and recipient
     */
    @GetMapping("/between/{senderId}/{recipientId}")
    public ResponseEntity<List<CommunicationPortal>> getMessagesBetweenUsers(@PathVariable Long senderId,
                                                                             @PathVariable Long recipientId) {
        try {
            List<CommunicationPortal> messages = messagesRepository.findBySenderIdAndRecipientId(senderId, recipientId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get messages for recipient with specific role
     */
    @GetMapping("/recipient/{recipientId}/role/{recipientRole}")
    public ResponseEntity<List<CommunicationPortal>> getMessagesByRecipientAndRole(@PathVariable Long recipientId,
                                                                                   @PathVariable CommunicationPortal.UserRole recipientRole) {
        try {
            List<CommunicationPortal> messages = messagesRepository.findByRecipientIdAndRecipientRole(recipientId, recipientRole);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get full conversation between two users
     */
    @GetMapping("/conversation/{user1Id}/{user2Id}")
    public ResponseEntity<List<CommunicationPortal>> getConversation(@PathVariable Long user1Id,
                                                                     @PathVariable Long user2Id) {
        try {
            List<CommunicationPortal> conversation = messagesRepository.findConversation(user1Id, user2Id);
            return ResponseEntity.ok(conversation);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get unread messages for recipient
     */
    @GetMapping("/recipient/{recipientId}/unread")
    public ResponseEntity<List<CommunicationPortal>> getUnreadMessages(@PathVariable Long recipientId) {
        try {
            List<CommunicationPortal> unreadMessages = messagesRepository.findByRecipientIdAndIsRead(recipientId, false);
            return ResponseEntity.ok(unreadMessages);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get unread messages ordered by sent date
     */
    @GetMapping("/recipient/{recipientId}/unread/ordered")
    public ResponseEntity<List<CommunicationPortal>> getUnreadMessagesOrdered(@PathVariable Long recipientId) {
        try {
            List<CommunicationPortal> unreadMessages = messagesRepository.findByRecipientIdAndIsReadOrderBySentAtDesc(recipientId, false);
            return ResponseEntity.ok(unreadMessages);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Mark message as read
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<CommunicationPortal> markAsRead(@PathVariable Long id) {
        try {
            Optional<CommunicationPortal> optionalMessage = messagesRepository.findById(id);
            if (optionalMessage.isPresent()) {
                CommunicationPortal message = optionalMessage.get();
                message.setIsRead(true);
                message.setReadAt(LocalDateTime.now());
                CommunicationPortal updatedMessage = messagesRepository.save(message);
                return ResponseEntity.ok(updatedMessage);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Mark multiple messages as read
     */
    @PatchMapping("/mark-read")
    public ResponseEntity<Void> markMultipleAsRead(@RequestBody List<Long> messageIds) {
        try {
            for (Long id : messageIds) {
                Optional<CommunicationPortal> optionalMessage = messagesRepository.findById(id);
                if (optionalMessage.isPresent()) {
                    CommunicationPortal message = optionalMessage.get();
                    message.setIsRead(true);
                    message.setReadAt(LocalDateTime.now());
                    messagesRepository.save(message);
                }
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * get all conversations for a user
     * so basically show all conversations with latest message on top like a normal messaging system
     */
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

    /**
     * get all messages from a convo
     */
    @GetMapping("/chat/conversation/{conversationId}")
    public ResponseEntity<?> getConversationMessages(@PathVariable String conversationId,
                                                      @RequestParam Long userId) {
        try {
            List<MessageDTO> messages = messageService.getConversationMessages(conversationId, userId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to load messages: " + e.getMessage()));
        }
    }

    /**
     * send a text message
     */
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

            MessageDTO sent = messageService.sendMessage(senderId, recipientId, messageText);
            return ResponseEntity.ok(sent);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to send message: " + e.getMessage()));
        }
    }

    /**
     * send a message with file/image
     */
    @PostMapping("/chat/send-with-attachment")
    public ResponseEntity<?> sendMessageWithAttachment(@RequestBody Map<String, Object> request) {
        try {
            Long senderId = Long.valueOf(request.get("senderId").toString());
            Long recipientId = Long.valueOf(request.get("recipientId").toString());
            String messageText = (String) request.get("message");
            String messageTypeStr = (String) request.get("messageType");
            String attachmentUrl = (String) request.get("attachmentUrl");
            String attachmentName = (String) request.get("attachmentName");

            // default to FILE if not specified
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

    /**
     * mark messages as read
     */
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

    /**
     * unread count noti
     */
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

    /**
     * get list of users to chat
     */
    @GetMapping("/chat/available-users")
    public ResponseEntity<?> getAvailableUsers(@RequestParam Long currentUserId) {
        try {
            List<UserProfile> users = messageService.getAvailableUsers(currentUserId);

            // map to simpler response (don't send full UserProfile)
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

    /**
     * start or get existing conversation with a user
     */
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