package com.clinicore.project.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MessagesControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createAndRetrieveMessage() throws Exception {
        String messageJson = """
            {
                "senderId": 1,
                "recipientId": 2,
                "message": "Integration test message",
                "senderRole": "CAREGIVER",
                "recipientRole": "RESIDENT",
                "subject": "Test Subject",
                "isRead": false
            }
            """;

        /**
         * Create message
         */
        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(messageJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Integration test message"))
                .andExpect(jsonPath("$.subject").value("Test Subject"))
                .andExpect(jsonPath("$.isRead").value(false))
                .andExpect(jsonPath("$.readAt").doesNotExist());

        /**
         * Get all messages
         */
        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        /**
         * Mark message as read and read_at is set
         */
        mockMvc.perform(patch("/api/messages/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isRead").value(true))
                .andExpect(jsonPath("$.readAt").exists());
    }
}