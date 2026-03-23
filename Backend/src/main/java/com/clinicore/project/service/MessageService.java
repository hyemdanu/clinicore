package com.clinicore.project.service;

import com.clinicore.project.dto.ConversationDTO;
import com.clinicore.project.dto.MessageDTO;
import com.clinicore.project.entity.CommunicationPortal;
import com.clinicore.project.entity.UserProfile;
import com.clinicore.project.repository.MessagesRepository;
import com.clinicore.project.repository.UserProfileRepository;
import com.clinicore.project.util.EncryptionUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.crypto.SecretKey;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final MessagesRepository messagesRepository;
    private final UserProfileRepository userProfileRepository;
    private final SecretKey secretKey;

    public MessageService(MessagesRepository messagesRepository,
                          UserProfileRepository userProfileRepository) {
        this.messagesRepository = messagesRepository;
        this.userProfileRepository = userProfileRepository;
        this.secretKey = EncryptionUtil.getKey(); // Updated method call
    }

    public List<ConversationDTO> getUserConversations(Long userId) {
        List<CommunicationPortal> latestMessages = messagesRepository.findUserConversations(userId);

        if (latestMessages.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> otherUserIds = new HashSet<>();
        for (CommunicationPortal msg : latestMessages) {
            Long otherUserId = msg.getSenderId().equals(userId) ? msg.getRecipientId() : msg.getSenderId();
            otherUserIds.add(otherUserId);
        }
        Map<Long, UserProfile> userMap = userProfileRepository.findAllById(otherUserIds).stream()
                .collect(Collectors.toMap(UserProfile::getId, Function.identity()));

        Map<String, Long> unreadMap = new HashMap<>();
        for (Object[] row : messagesRepository.countUnreadByConversation(userId)) {
            unreadMap.put((String) row[0], (Long) row[1]);
        }

        List<ConversationDTO> conversations = new ArrayList<>();

        for (CommunicationPortal msg : latestMessages) {
            ConversationDTO dto = new ConversationDTO();
            dto.setConversationId(msg.getConversationId());

            Long otherUserId = msg.getSenderId().equals(userId) ? msg.getRecipientId() : msg.getSenderId();
            dto.setOtherUserId(otherUserId);

            UserProfile otherUser = userMap.get(otherUserId);
            if (otherUser != null) {
                dto.setOtherUserName(otherUser.getFirstName() + " " + otherUser.getLastName());
                dto.setOtherUserRole(otherUser.getRole() != null ? otherUser.getRole().name() : null);
            }

            dto.setLastMessage(msg.getMessage());
            dto.setLastMessageType(msg.getMessageType() != null ? msg.getMessageType().name() : "TEXT");
            dto.setLastMessageAt(msg.getSentAt());
            dto.setLastMessageSenderId(msg.getSenderId());

            Long unread = unreadMap.getOrDefault(msg.getConversationId(), 0L);
            dto.setUnreadCount(unread.intValue());

            conversations.add(dto);
        }

        return conversations;
    }

    public List<MessageDTO> getConversationMessages(String conversationId) {
        List<CommunicationPortal> messages = messagesRepository.findByConversationIdOrderBySentAtAsc(conversationId);

        if (messages.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> userIds = new HashSet<>();
        for (CommunicationPortal msg : messages) {
            userIds.add(msg.getSenderId());
            userIds.add(msg.getRecipientId());
        }
        Map<Long, UserProfile> userMap = userProfileRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserProfile::getId, Function.identity()));

        return messages.stream()
                .map(msg -> {
                    UserProfile sender = userMap.get(msg.getSenderId());
                    UserProfile recipient = userMap.get(msg.getRecipientId());
                    String senderName = sender != null ? sender.getFirstName() + " " + sender.getLastName() : null;
                    String recipientName = recipient != null ? recipient.getFirstName() + " " + recipient.getLastName() : null;
                    return MessageDTO.fromEntityWithNames(msg, senderName, recipientName);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageDTO sendMessage(Long senderId, Long recipientId, String messageText) {
        UserProfile sender = userProfileRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        UserProfile recipient = userProfileRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        CommunicationPortal message = new CommunicationPortal();

        // Encrypt message
        try {
            String encryptedMessage = EncryptionUtil.encrypt(messageText, secretKey);
            message.setMessage(encryptedMessage);
        } catch (Exception e) {
            // Handle error
        }

        message.setSenderId(senderId);
        message.setSenderRole(convertRole(sender.getRole()));
        message.setRecipientId(recipientId);
        message.setRecipientRole(convertRole(recipient.getRole()));
        message.setMessageType(CommunicationPortal.MessageType.TEXT);

        CommunicationPortal saved = messagesRepository.save(message);

        String senderName = sender.getFirstName() + " " + sender.getLastName();
        String recipientName = recipient.getFirstName() + " " + recipient.getLastName();

        return MessageDTO.fromEntityWithNames(saved, senderName, recipientName);
    }

    @Transactional
    public MessageDTO sendMessageWithAttachment(Long senderId, Long recipientId,
                                                 String messageText,
                                                 CommunicationPortal.MessageType messageType,
                                                 String attachmentUrl,
                                                 String attachmentName) {
        UserProfile sender = userProfileRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        UserProfile recipient = userProfileRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        CommunicationPortal message = new CommunicationPortal();

        // Encrypt message
        try {
            String encryptedMessage = EncryptionUtil.encrypt(messageText, secretKey);
            message.setMessage(encryptedMessage);
        } catch (Exception e) {
            // Handle error
        }

        message.setSenderId(senderId);
        message.setSenderRole(convertRole(sender.getRole()));
        message.setRecipientId(recipientId);
        message.setRecipientRole(convertRole(recipient.getRole()));
        message.setMessageType(messageType);
        message.setAttachmentUrl(attachmentUrl);
        message.setAttachmentName(attachmentName);

        CommunicationPortal saved = messagesRepository.save(message);

        String senderName = sender.getFirstName() + " " + sender.getLastName();
        String recipientName = recipient.getFirstName() + " " + recipient.getLastName();

        return MessageDTO.fromEntityWithNames(saved, senderName, recipientName);
    }

    @Transactional
    public void markConversationAsRead(String conversationId, Long userId) {
        messagesRepository.bulkMarkAsRead(conversationId, userId);
    }

    public Integer getTotalUnreadCount(Long userId) {
        Integer count = messagesRepository.countTotalUnread(userId);
        return count != null ? count : 0;
    }

    public List<UserProfile> getAvailableUsers(Long currentUserId) {
        if (currentUserId == null) {
            throw new RuntimeException("Current user ID is required");
        }

        userProfileRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user not found with ID: " + currentUserId));

        return userProfileRepository.findAll().stream()
                .filter(user -> user.getId() != null && !user.getId().equals(currentUserId))
                .collect(Collectors.toList());
    }

    // same values, different enums
    private CommunicationPortal.UserRole convertRole(UserProfile.Role role) {
        if (role == null) return null;
        return CommunicationPortal.UserRole.valueOf(role.name());
    }

    public String generateConversationId(long userId1, long userId2) {
        long smaller = Math.min(userId1, userId2);
        long larger = Math.max(userId1, userId2);
        return smaller + "_" + larger;
    }
}
