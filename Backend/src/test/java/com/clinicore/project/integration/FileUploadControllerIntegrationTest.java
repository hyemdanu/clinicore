package com.clinicore.project.integration;

import com.clinicore.project.service.FileUploadService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class FileUploadControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;

    // FileUploadService writes to disk — mock it to keep tests filesystem-free
    @MockitoBean private FileUploadService fileUploadService;

    @MockitoBean private com.clinicore.project.service.JwtService jwtService;
    @MockitoBean private com.clinicore.project.config.JwtAuthenticationFilter jwtAuthenticationFilter;

    // TEST 1: valid PDF upload returns success response with url, originalName, type
    @Test
    @Order(1)
    @DisplayName("TEST 1: POST valid PDF returns 200 with url and metadata")
    void testUploadValidPdfReturns200() throws Exception {
        when(fileUploadService.uploadFile(any()))
                .thenReturn(new FileUploadService.FileUploadResult(
                        "/uploads/messages/test.pdf", "test.pdf", "FILE"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "fake pdf content".getBytes());

        mockMvc.perform(multipart("/api/upload/chat").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.url", is("/uploads/messages/test.pdf")))
                .andExpect(jsonPath("$.originalName", is("test.pdf")))
                .andExpect(jsonPath("$.type", is("FILE")));
    }

    // TEST 2: valid image upload returns type IMAGE
    @Test
    @Order(2)
    @DisplayName("TEST 2: POST valid image returns 200 with type IMAGE")
    void testUploadValidImageReturns200() throws Exception {
        when(fileUploadService.uploadFile(any()))
                .thenReturn(new FileUploadService.FileUploadResult(
                        "/uploads/messages/photo.jpg", "photo.jpg", "IMAGE"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF});

        mockMvc.perform(multipart("/api/upload/chat").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.type", is("IMAGE")));
    }

    // TEST 3: empty file — service throws IllegalArgumentException → controller returns 400
    @Test
    @Order(3)
    @DisplayName("TEST 3: POST empty file returns 400")
    void testUploadEmptyFileReturns400() throws Exception {
        when(fileUploadService.uploadFile(any()))
                .thenThrow(new IllegalArgumentException("File is empty"));

        MockMultipartFile empty = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]);

        mockMvc.perform(multipart("/api/upload/chat").file(empty))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error", is("File is empty")));
    }

    // TEST 4: disallowed content type — service throws IllegalArgumentException → 400
    @Test
    @Order(4)
    @DisplayName("TEST 4: POST disallowed file type returns 400")
    void testUploadDisallowedTypeReturns400() throws Exception {
        when(fileUploadService.uploadFile(any()))
                .thenThrow(new IllegalArgumentException("File type not allowed: application/octet-stream"));

        MockMultipartFile exe = new MockMultipartFile(
                "file", "bad.exe", "application/octet-stream", new byte[]{0x4D, 0x5A});

        mockMvc.perform(multipart("/api/upload/chat").file(exe))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error", is("File type not allowed: application/octet-stream")));
    }

    // TEST 5: DELETE existing file → service returns true → 200
    @Test
    @Order(5)
    @DisplayName("TEST 5: DELETE existing file returns 200")
    void testDeleteExistingFileReturns200() throws Exception {
        when(fileUploadService.deleteFile("/uploads/messages/test.pdf")).thenReturn(true);

        mockMvc.perform(delete("/api/upload/chat")
                        .param("fileUrl", "/uploads/messages/test.pdf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    // TEST 6: DELETE non-existent file → service returns false → 404
    @Test
    @Order(6)
    @DisplayName("TEST 6: DELETE non-existent file returns 404")
    void testDeleteNonExistentFileReturns404() throws Exception {
        when(fileUploadService.deleteFile(any())).thenReturn(false);

        mockMvc.perform(delete("/api/upload/chat")
                        .param("fileUrl", "/uploads/messages/ghost.pdf"))
                .andExpect(status().isNotFound());
    }
}
