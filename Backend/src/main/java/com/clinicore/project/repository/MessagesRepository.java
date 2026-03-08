package com.clinicore.project.repository;

import com.clinicore.project.entity.CommunicationPortal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessagesRepository extends JpaRepository<CommunicationPortal, Long> {

    List<CommunicationPortal> findBySenderId(Long senderId);

    List<CommunicationPortal> findByRecipientId(Long recipientId);

    List<CommunicationPortal> findBySenderRole(CommunicationPortal.UserRole senderRole);

    List<CommunicationPortal> findBySenderIdAndRecipientId(Long senderId, Long recipientId);

    List<CommunicationPortal> findByRecipientIdAndRecipientRole(Long recipientId, CommunicationPortal.UserRole recipientRole);

    @Query("SELECT cp FROM CommunicationPortal cp WHERE " +
            "(cp.senderId = :user1Id AND cp.recipientId = :user2Id) OR " +
            "(cp.senderId = :user2Id AND cp.recipientId = :user1Id) " +
            "ORDER BY cp.sentAt ASC")
    List<CommunicationPortal> findConversation(@Param("user1Id") Long user1Id,
                                               @Param("user2Id") Long user2Id);

    List<CommunicationPortal> findByRecipientIdAndIsRead(Long recipientId, Boolean isRead);

    List<CommunicationPortal> findByRecipientIdAndIsReadOrderBySentAtDesc(Long recipientId, Boolean isRead);

    List<CommunicationPortal> findByConversationIdOrderBySentAtAsc(String conversationId);

    @Query("SELECT cp FROM CommunicationPortal cp WHERE cp.id IN " +
            "(SELECT MAX(cp2.id) FROM CommunicationPortal cp2 " +
            "WHERE cp2.senderId = :userId OR cp2.recipientId = :userId " +
            "GROUP BY cp2.conversationId) " +
            "ORDER BY cp.sentAt DESC")
    List<CommunicationPortal> findUserConversations(@Param("userId") Long userId);

    @Query("SELECT COUNT(cp) FROM CommunicationPortal cp " +
            "WHERE cp.recipientId = :userId AND cp.isRead = false")
    Integer countTotalUnread(@Param("userId") Long userId);

    // batch: unread counts grouped by conversation
    @Query("SELECT cp.conversationId, COUNT(cp) FROM CommunicationPortal cp " +
            "WHERE cp.recipientId = :userId AND cp.isRead = false " +
            "GROUP BY cp.conversationId")
    List<Object[]> countUnreadByConversation(@Param("userId") Long userId);

    // bulk mark as read in one query
    @Modifying
    @Query("UPDATE CommunicationPortal cp SET cp.isRead = true, cp.readAt = CURRENT_TIMESTAMP " +
            "WHERE cp.conversationId = :conversationId AND cp.recipientId = :userId AND cp.isRead = false")
    void bulkMarkAsRead(@Param("conversationId") String conversationId, @Param("userId") Long userId);
}