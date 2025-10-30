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
}