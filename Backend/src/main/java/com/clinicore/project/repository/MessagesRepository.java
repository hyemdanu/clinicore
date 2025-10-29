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
    List<CommunicationPortal> findBySenderRole(String senderRole);

    /**
     * Get communications between a specific sender and recipient
     */
    List<CommunicationPortal> findBySenderIdAndRecipientId(Long senderId, Long recipientId);

    /**
     * Get communications for a recipient with a specific role
     */
    List<CommunicationPortal> findByRecipientIdAndRecipientRole(Long recipientId, String recipientRole);

    /**
     * Query to get full conversation between two users in order
     */
    @Query("SELECT cp FROM CommunicationPortal cp WHERE " +
            "(cp.sender_id = :user1Id AND cp.recipient_id = :user2Id) OR " +
            "(cp.sender_id = :user2Id AND cp.recipient_id = :user1Id) " +
            "ORDER BY cp.sent_date ASC, cp.sent_time ASC")

    /**
     * Get full conversation between two users in order
     */
    List<CommunicationPortal> findConversation(@Param("user1Id") Long user1Id,
                                               @Param("user2Id") Long user2Id);
}