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
    List<CommunicationPortal> findBySender_id(Long senderId);

    /**
     * Get all communications sent to a specific recipient
     */
    List<CommunicationPortal> findByRecipient_id(Long recipientId);

    /**
     * Get communications sent by users with specific role
     */
    List<CommunicationPortal> findBySender_role(CommunicationPortal.UserRole senderRole);

    /**
     * Get communications between a specific sender and recipient
     */
    List<CommunicationPortal> findBySender_idAndRecipient_id(Long senderId, Long recipientId);

    /**
     * Get communications for a recipient with a specific role
     */
    List<CommunicationPortal> findByRecipient_idAndRecipient_role(Long recipientId, CommunicationPortal.UserRole recipientRole);

    /**
     * Get full conversation between two users in order
     */
    @Query("SELECT cp FROM CommunicationPortal cp WHERE " +
            "(cp.sender_id = :user1Id AND cp.recipient_id = :user2Id) OR " +
            "(cp.sender_id = :user2Id AND cp.recipient_id = :user1Id) " +
            "ORDER BY cp.sent_at ASC")
    List<CommunicationPortal> findConversation(@Param("user1Id") Long user1Id,
                                               @Param("user2Id") Long user2Id);

    /**
     * Get unread messages for a specific recipient
     */
    List<CommunicationPortal> findByRecipient_idAndIs_read(Long recipientId, Boolean isRead);

    /**
     * Get unread messages ordered by sent date
     */
    List<CommunicationPortal> findByRecipient_idAndIs_readOrderBySent_atDesc(Long recipientId, Boolean isRead);
}