package com.clinicore.project.repository;

import com.clinicore.project.entity.CommunicationPortal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessagesRepository extends JpaRepository<CommunicationPortal, Long> {

    /**
     * Get all communications for a specific sender
     */
    List<CommunicationPortal> findBySenderId(Long senderId);

    /**
     * Get all communications sent to a specific recipient
     */
    List<CommunicationPortal> findByRecipientId(Long recipientId);

    /**
     * Get communications sent by users with specific role
     */
    List<CommunicationPortal> findBySenderRole(CommunicationPortal.UserRole senderRole);

    /**
     * Get communications between a specific sender and recipient
     */
    List<CommunicationPortal> findBySenderIdAndRecipientId(Long senderId, Long recipientId);

    /**
     * Get communications for a recipient with a specific role
     */
    List<CommunicationPortal> findByRecipientIdAndRecipientRole(Long recipientId, CommunicationPortal.UserRole recipientRole);

    /**
     * Get full conversation between two users in order
     */
    @Query("SELECT cp FROM CommunicationPortal cp WHERE " +
            "(cp.senderId = :user1Id AND cp.recipientId = :user2Id) OR " +
            "(cp.senderId = :user2Id AND cp.recipientId = :user1Id) " +
            "ORDER BY cp.sentAt ASC")
    List<CommunicationPortal> findConversation(@Param("user1Id") Long user1Id,
                                               @Param("user2Id") Long user2Id);

    /**
     * Get unread messages for a specific recipient
     */
    List<CommunicationPortal> findByRecipientIdAndIsRead(Long recipientId, Boolean isRead);

    /**
     * Get unread messages ordered by sent date
     */
    List<CommunicationPortal> findByRecipientIdAndIsReadOrderBySentAtDesc(Long recipientId, Boolean isRead);

    /**
     * get messages by conversationId ordered by time
     */
    List<CommunicationPortal> findByConversationIdOrderBySentAtAsc(String conversationId);

    /**
     * get all unique conversations a user is part of
     */
    @Query("SELECT cp FROM CommunicationPortal cp WHERE cp.id IN " +
            "(SELECT MAX(cp2.id) FROM CommunicationPortal cp2 " +
            "WHERE cp2.senderId = :userId OR cp2.recipientId = :userId " +
            "GROUP BY cp2.conversationId) " +
            "ORDER BY cp.sentAt DESC")
    List<CommunicationPortal> findUserConversations(@Param("userId") Long userId);

    /**
     * count unread messages in a specific conversation for a user
     */
    @Query("SELECT COUNT(cp) FROM CommunicationPortal cp " +
            "WHERE cp.conversationId = :conversationId " +
            "AND cp.recipientId = :userId " +
            "AND cp.isRead = false")
    Integer countUnreadInConversation(@Param("conversationId") String conversationId,
                                      @Param("userId") Long userId);

    /**
     * count total unread messages for a user across all conversations
     */
    @Query("SELECT COUNT(cp) FROM CommunicationPortal cp " +
            "WHERE cp.recipientId = :userId AND cp.isRead = false")
    Integer countTotalUnread(@Param("userId") Long userId);

    /**
     * mark all messages in a conversation as read for a user
     */
    @Query("SELECT cp FROM CommunicationPortal cp " +
            "WHERE cp.conversationId = :conversationId " +
            "AND cp.recipientId = :userId " +
            "AND cp.isRead = false")
    List<CommunicationPortal> findUnreadInConversation(@Param("conversationId") String conversationId,
                                                       @Param("userId") Long userId);
}