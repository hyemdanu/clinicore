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

    // attachment-only projection: avoids loading the full entity (including other BLOB columns) when streaming
    interface AttachmentView {
        Long getId();
        Long getSenderId();
        Long getRecipientId();
        String getAttachmentName();
        String getAttachmentType();
        byte[] getAttachmentData();
    }

    @Query("SELECT cp.id AS id, cp.senderId AS senderId, cp.recipientId AS recipientId, " +
            "cp.attachmentName AS attachmentName, cp.attachmentType AS attachmentType, " +
            "cp.attachmentData AS attachmentData " +
            "FROM CommunicationPortal cp WHERE cp.id = :id")
    AttachmentView findAttachmentById(@Param("id") Long id);
}