package com.clinicore.project.service;

import com.clinicore.project.dto.ConversationDTO;
import com.clinicore.project.dto.MessageDTO;
import com.clinicore.project.entity.CommunicationPortal;
import com.clinicore.project.entity.UserProfile;
import com.clinicore.project.repository.MessagesRepository;
import com.clinicore.project.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * message service handing message logic
 */
@Service
public class MessageService {

    private final MessagesRepository messagesRepository;
    private final UserProfileRepository userProfileRepository;

    public MessageService(MessagesRepository messagesRepository,
                         UserProfileRepository userProfileRepository) {
        this.messagesRepository = messagesRepository;
        this.userProfileRepository = userProfileRepository;
    }

    /**
     * get all conversations for a user
     * returns list with last message preview and unread count
     */
    public List<ConversationDTO> getUserConversations(Long userId) {
        // get the latest message from each conversation
        List<CommunicationPortal> latestMessages = messagesRepository.findUserConversations(userId);

        List<ConversationDTO> conversations = new ArrayList<>();

        for (CommunicationPortal msg : latestMessages) {
            ConversationDTO dto = new ConversationDTO();
            dto.setConversationId(msg.getConversationId());

            // figure out who the other person is
            Long otherUserId = msg.getSenderId().equals(userId) ? msg.getRecipientId() : msg.getSenderId();
            dto.setOtherUserId(otherUserId);

            // get the other person's name and role
            UserProfile otherUser = userProfileRepository.findById(otherUserId).orElse(null);
            if (otherUser != null) {
                dto.setOtherUserName(otherUser.getFirstName() + " " + otherUser.getLastName());
                dto.setOtherUserRole(otherUser.getRole() != null ? otherUser.getRole().name() : null);
            }

            // last message preview
            dto.setLastMessage(msg.getMessage());
            dto.setLastMessageType(msg.getMessageType() != null ? msg.getMessageType().name() : "TEXT");
            dto.setLastMessageAt(msg.getSentAt());
            dto.setLastMessageSenderId(msg.getSenderId());

            // count unread in this conversation
            Integer unreadCount = messagesRepository.countUnreadInConversation(msg.getConversationId(), userId);
            dto.setUnreadCount(unreadCount != null ? unreadCount : 0);

            conversations.add(dto);
        }

        return conversations;
    }

    /**
     * get all messages in a conversation
     */
    public List<MessageDTO> getConversationMessages(String conversationId) {
        List<CommunicationPortal> messages = messagesRepository.findByConversationIdOrderBySentAtAsc(conversationId);

        return messages.stream()
                .map(msg -> {
                    // get sender and recipient names
                    String senderName = null;
                    String recipientName = null;

                    UserProfile sender = userProfileRepository.findById(msg.getSenderId()).orElse(null);
                    if (sender != null) {
                        senderName = sender.getFirstName() + " " + sender.getLastName();
                    }

                    UserProfile recipient = userProfileRepository.findById(msg.getRecipientId()).orElse(null);
                    if (recipient != null) {
                        recipientName = recipient.getFirstName() + " " + recipient.getLastName();
                    }

                    return MessageDTO.fromEntityWithNames(msg, senderName, recipientName);
                })
                .collect(Collectors.toList());
    }

    /**
     * send a text message
     */
    @Transactional
    public MessageDTO sendMessage(Long senderId, Long recipientId, String messageText) {
        // get sender and recipient info
        UserProfile sender = userProfileRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        UserProfile recipient = userProfileRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        CommunicationPortal message = new CommunicationPortal();
        message.setSenderId(senderId);
        message.setSenderRole(convertRole(sender.getRole()));
        message.setRecipientId(recipientId);
        message.setRecipientRole(convertRole(recipient.getRole()));
        message.setMessage(messageText);
        message.setMessageType(CommunicationPortal.MessageType.TEXT);
        // conversationId is auto-generated in @PrePersist

        CommunicationPortal saved = messagesRepository.save(message);

        String senderName = sender.getFirstName() + " " + sender.getLastName();
        String recipientName = recipient.getFirstName() + " " + recipient.getLastName();

        return MessageDTO.fromEntityWithNames(saved, senderName, recipientName);
    }

    /**
     * send a message with attachment (image or file)
     */
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
        message.setSenderId(senderId);
        message.setSenderRole(convertRole(sender.getRole()));
        message.setRecipientId(recipientId);
        message.setRecipientRole(convertRole(recipient.getRole()));
        message.setMessage(messageText);
        message.setMessageType(messageType);
        message.setAttachmentUrl(attachmentUrl);
        message.setAttachmentName(attachmentName);

        CommunicationPortal saved = messagesRepository.save(message);

        String senderName = sender.getFirstName() + " " + sender.getLastName();
        String recipientName = recipient.getFirstName() + " " + recipient.getLastName();

        return MessageDTO.fromEntityWithNames(saved, senderName, recipientName);
    }

    /**
     * mark all messages in a conversation as read for a user
     */
    @Transactional
    public void markConversationAsRead(String conversationId, Long userId) {
        List<CommunicationPortal> unread = messagesRepository.findUnreadInConversation(conversationId, userId);

        for (CommunicationPortal msg : unread) {
            msg.markAsRead();
        }

        messagesRepository.saveAll(unread);
    }

    /**
     * get total unread count for a user for notis
     */
    public Integer getTotalUnreadCount(Long userId) {
        Integer count = messagesRepository.countTotalUnread(userId);
        return count != null ? count : 0;
    }

    /**
     * get users available to chat with
     * returns all users except the current user
     */
    public List<UserProfile> getAvailableUsers(Long currentUserId) {
        // verify current user exists first
        if (currentUserId == null) {
            throw new RuntimeException("Current user ID is required");
        }

        userProfileRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user not found with ID: " + currentUserId));

        return userProfileRepository.findAll().stream()
                .filter(user -> user.getId() != null && !user.getId().equals(currentUserId))
                .collect(Collectors.toList());
    }

    /**
     * convert UserProfile.Role to CommunicationPortal.UserRole
     * they're the same values but different enums so we gotta convert
     */
    private CommunicationPortal.UserRole convertRole(UserProfile.Role role) {
        if (role == null) return null;
        return CommunicationPortal.UserRole.valueOf(role.name());
    }

    /**
     * generate conversationId from two user IDs
     */
    public String generateConversationId(long userId1, long userId2) {
        long smaller = Math.min(userId1, userId2);
        long larger = Math.max(userId1, userId2);
        return smaller + "_" + larger;
    }
}
