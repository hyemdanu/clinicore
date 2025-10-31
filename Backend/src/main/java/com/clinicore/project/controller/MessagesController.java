package com.clinicore.project.controller;

import com.clinicore.project.entity.CommunicationPortal;
import com.clinicore.project.repository.MessagesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*") // Adjust based on your frontend URL
public class MessagesController {

    @Autowired
    private MessagesRepository messagesRepository;

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

                // Update fields as needed
                if (messageDetails.getContent() != null) {
                    message.setContent(messageDetails.getContent());
                }
                if (messageDetails.getIsRead() != null) {
                    message.setIsRead(messageDetails.getIsRead());
                }
                // Add other fields as necessary

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
                    messagesRepository.save(message);
                }
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}