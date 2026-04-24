package com.clinicore.project.integration;

import com.clinicore.project.entity.CommunicationPortal;
import com.clinicore.project.repository.MessagesRepository;
import com.clinicore.project.service.EncryptionService;
import com.clinicore.project.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests verifying AES-256-GCM encryption end-to-end for chat messages.
 * Requires users with id=1 (Admin/Kevin Nguyen) and id=2 (Caregiver/Emily Tran) in the DB.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MessageEncryptionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessagesRepository messagesRepository;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private JwtService jwtService;

    // sender=1 (Admin), recipient=2 (Caregiver) → conversationId = "1_2"
    private static final Long SENDER_ID = 1L;
    private static final Long RECIPIENT_ID = 2L;
    private static final String CONVERSATION_ID = "1_2";

    private String authHeader;

    @BeforeEach
    void setUpAuth() {
        String token = jwtService.generateToken(SENDER_ID, "admin", "ROLE_ADMIN");
        authHeader = "Bearer " + token;
    }

    // --- Scenario 1 ---

    @Test
    @DisplayName("Sent message is stored encrypted — stored content must not equal plaintext")
    void sentMessageIsStoredEncrypted() throws Exception {
        String plaintext = "Hello encryption scenario one";

        mockMvc.perform(post("/api/messages/chat/send")
                        .header(HttpHeaders.AUTHORIZATION, authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"senderId": 1, "recipientId": 2, "message": "%s"}
                            """.formatted(plaintext)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(plaintext));

        List<CommunicationPortal> stored =
                messagesRepository.findByConversationIdOrderBySentAtAsc(CONVERSATION_ID);
        assertThat(stored).isNotEmpty();

        CommunicationPortal last = stored.getLast();
        assertThat(last.getMessage())
                .isNotNull()
                .isNotEqualTo(plaintext);
    }

    // --- Scenario 2 ---

    @Test
    @DisplayName("Fetching conversation decrypts back to plaintext")
    void fetchConversationDecryptsToPlaintext() throws Exception {
        String plaintext = "Decryption round-trip test";

        mockMvc.perform(post("/api/messages/chat/send")
                        .header(HttpHeaders.AUTHORIZATION, authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"senderId": 1, "recipientId": 2, "message": "%s"}
                            """.formatted(plaintext)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/messages/chat/conversation/" + CONVERSATION_ID)
                        .header(HttpHeaders.AUTHORIZATION, authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].message", hasItem(plaintext)));
    }

    // --- Scenario 3 ---

    @Test
    @DisplayName("Same plaintext sent twice produces different ciphertexts (random IV)")
    void samePlaintextProducesDifferentCiphertexts() throws Exception {
        String plaintext = "Identical plaintext, unique ciphertext";
        String body = """
            {"senderId": 1, "recipientId": 2, "message": "%s"}
            """.formatted(plaintext);

        mockMvc.perform(post("/api/messages/chat/send")
                        .header(HttpHeaders.AUTHORIZATION, authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/messages/chat/send")
                        .header(HttpHeaders.AUTHORIZATION, authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        List<CommunicationPortal> stored =
                messagesRepository.findByConversationIdOrderBySentAtAsc(CONVERSATION_ID);
        assertThat(stored.size()).isGreaterThanOrEqualTo(2);

        int size = stored.size();
        String cipher1 = stored.get(size - 2).getMessage();
        String cipher2 = stored.get(size - 1).getMessage();
        assertThat(cipher1).isNotEqualTo(cipher2);
    }

    // --- Scenario 4 ---

    @Test
    @DisplayName("Legacy plaintext rows are readable — decryptSafe falls back gracefully")
    void legacyPlaintextRowsAreReadable() throws Exception {
        String legacyPlaintext = "This row was stored before encryption was enabled";

        // Bypass the service and insert the plaintext directly to simulate a pre-encryption row
        CommunicationPortal legacy = new CommunicationPortal();
        legacy.setSenderId(SENDER_ID);
        legacy.setRecipientId(RECIPIENT_ID);
        legacy.setSenderRole(CommunicationPortal.UserRole.ADMIN);
        legacy.setRecipientRole(CommunicationPortal.UserRole.CAREGIVER);
        legacy.setMessage(legacyPlaintext);
        legacy.setMessageType(CommunicationPortal.MessageType.TEXT);
        messagesRepository.save(legacy);

        // The fetch endpoint runs through decryptSafe which must return plaintext unchanged
        mockMvc.perform(get("/api/messages/chat/conversation/" + CONVERSATION_ID)
                        .header(HttpHeaders.AUTHORIZATION, authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].message", hasItem(legacyPlaintext)));
    }

    // --- Scenario 5 ---

    @Test
    @DisplayName("sendMessageWithAttachment also encrypts the message field")
    void messageWithAttachmentIsEncrypted() throws Exception {
        // minimal PNG signature — enough to pass content-type detection
        byte[] pngBytes = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        MockMultipartFile file = new MockMultipartFile(
                "file", "test-encrypt.png", "image/png", pngBytes);

        mockMvc.perform(multipart("/api/messages/chat/send-with-attachment")
                        .file(file)
                        .param("senderId", "1")
                        .param("recipientId", "2")
                        .param("message", "")
                        .header(HttpHeaders.AUTHORIZATION, authHeader))
                .andExpect(status().isOk());

        List<CommunicationPortal> stored =
                messagesRepository.findByConversationIdOrderBySentAtAsc(CONVERSATION_ID);
        assertThat(stored).isNotEmpty();

        CommunicationPortal last = stored.getLast();
        // Encrypted empty string is NOT empty — it's IV + GCM auth tag encoded as base64
        assertThat(last.getMessage())
                .isNotNull()
                .isNotEmpty();
        // And it must decrypt back cleanly
        assertThat(encryptionService.decrypt(last.getMessage())).isEqualTo("");
        // And the binary payload landed in the DB
        assertThat(last.getAttachmentData()).isNotNull().isNotEmpty();
        assertThat(last.getAttachmentName()).isEqualTo("test-encrypt.png");
        assertThat(last.getAttachmentType()).isEqualTo("image/png");
    }

    // --- Scenario 6 ---

    @Test
    @DisplayName("getUserConversations returns decrypted last-message previews")
    void getUserConversationsReturnsDecryptedPreviews() throws Exception {
        String plaintext = "Preview decryption scenario six";

        mockMvc.perform(post("/api/messages/chat/send")
                        .header(HttpHeaders.AUTHORIZATION, authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"senderId": 1, "recipientId": 2, "message": "%s"}
                            """.formatted(plaintext)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/messages/chat/conversations?userId=" + SENDER_ID)
                        .header(HttpHeaders.AUTHORIZATION, authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].lastMessage", hasItem(plaintext)));
    }
}
