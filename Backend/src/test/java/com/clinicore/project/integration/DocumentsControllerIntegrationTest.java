package com.clinicore.project.integration;

import com.clinicore.project.entity.Document;
import com.clinicore.project.repository.DocumentsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class DocumentsControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private DocumentsRepository documentsRepository;

    @MockitoBean private com.clinicore.project.service.JwtService jwtService;
    @MockitoBean private com.clinicore.project.config.JwtAuthenticationFilter jwtAuthenticationFilter;

    private static Long uploadedDocumentId;

    private static final Long ADMIN_USER_ID       = 1L;  // must be ADMIN in test DB
    private static final Long EXISTING_RESIDENT_ID = 4L; // must exist in resident table

    private static final byte[] MINIMAL_PDF_BYTES = {
            0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x34  // %PDF-1.4
    };

    // TEST 1: /list returns DocumentMetadata projection — blob fields must be absent
    @Test
    @Order(1)
    @DisplayName("TEST 1: GET all documents returns metadata only (no LONGBLOB)")
    void testListDocumentsReturnsMetadataOnly() throws Exception {
        mockMvc.perform(get("/api/documents/list").param("userId", String.valueOf(ADMIN_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(List.class)))
                .andExpect(jsonPath("$[*].id",         everyItem(notNullValue())))
                .andExpect(jsonPath("$[*].title",      everyItem(notNullValue())))
                .andExpect(jsonPath("$[*].type",       everyItem(notNullValue())))
                .andExpect(jsonPath("$[*].residentId", everyItem(notNullValue())))
                .andExpect(jsonPath("$[*].document").doesNotExist())
                .andExpect(jsonPath("$[*].data").doesNotExist());
    }

    // TEST 2: POST upload persists the document; captures id for downstream tests
    @Test
    @Order(2)
    @DisplayName("TEST 2: POST upload persists document and returns documentId")
    void testUploadDocumentPersists() throws Exception {
        MockMultipartFile pdf = new MockMultipartFile("file", "test.pdf", "application/pdf", MINIMAL_PDF_BYTES);

        String body = mockMvc.perform(multipart("/api/documents/upload")
                        .file(pdf)
                        .param("currentUserId", String.valueOf(ADMIN_USER_ID))
                        .param("residentId",    String.valueOf(EXISTING_RESIDENT_ID))
                        .param("title",         "Integration Test Doc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId", notNullValue()))
                .andExpect(jsonPath("$.message", is("Document uploaded successfully")))
                .andReturn().getResponse().getContentAsString();

        uploadedDocumentId = objectMapper.readTree(body).get("documentId").asLong();

        Optional<Document> saved = documentsRepository.findById(uploadedDocumentId);
        assertTrue(saved.isPresent());
        assertEquals("Integration Test Doc", saved.get().getTitle());
        assertEquals(EXISTING_RESIDENT_ID, saved.get().getResidentId());
    }

    // TEST 3: /file/{id} returns raw binary; PDF magic bytes verified in response body
    @Test
    @Order(3)
    @DisplayName("TEST 3: GET document file by id returns binary content")
    void testGetDocumentFileReturnsBinaryContent() throws Exception {
        assertNotNull(uploadedDocumentId, "uploadedDocumentId must be set by TEST 2");

        byte[] bytes = mockMvc.perform(get("/api/documents/file/{id}", uploadedDocumentId)
                        .param("userId", String.valueOf(ADMIN_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")))
                .andExpect(header().string("Content-Disposition", containsString("Integration Test Doc")))
                .andReturn().getResponse().getContentAsByteArray();

        assertTrue(bytes.length >= 4);
        assertEquals(0x25, bytes[0] & 0xFF);
        assertEquals(0x50, bytes[1] & 0xFF);
        assertEquals(0x44, bytes[2] & 0xFF);
        assertEquals(0x46, bytes[3] & 0xFF);
    }

    // TEST 4: non-existent id → controller catches IAE and returns 403 (single catch block for both not-found and permission errors)
    @Test
    @Order(4)
    @DisplayName("TEST 4: GET non-existent document id returns 403")
    void testGetNonExistentDocumentReturns403() throws Exception {
        mockMvc.perform(get("/api/documents/file/{id}", 99999L)
                        .param("userId", String.valueOf(ADMIN_USER_ID)))
                .andExpect(status().isForbidden());
    }

    // TEST 5: DELETE removes the row; confirmed via repository lookup
    @Test
    @Order(5)
    @DisplayName("TEST 5: DELETE removes the document from the database")
    void testDeleteDocumentRemovesIt() throws Exception {
        assertNotNull(uploadedDocumentId, "uploadedDocumentId must be set by TEST 2");

        mockMvc.perform(delete("/api/documents/{id}", uploadedDocumentId)
                        .param("userId", String.valueOf(ADMIN_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Document deleted successfully")));

        assertFalse(documentsRepository.findById(uploadedDocumentId).isPresent());
    }

    // TEST 6: resident-scoped list uses DocumentMetadata projection; every row belongs to the requested resident
    @Test
    @Order(6)
    @DisplayName("TEST 6: GET documents by resident returns only that resident's docs")
    void testGetDocumentsForResidentReturnsOnlyThatResidentsDocs() throws Exception {
        // Upload a fresh doc for the resident since TEST 5 deleted the previous one
        MockMultipartFile pdf = new MockMultipartFile("file", "test2.pdf", "application/pdf", MINIMAL_PDF_BYTES);
        String body = mockMvc.perform(multipart("/api/documents/upload")
                        .file(pdf)
                        .param("currentUserId", String.valueOf(ADMIN_USER_ID))
                        .param("residentId",    String.valueOf(EXISTING_RESIDENT_ID))
                        .param("title",         "Resident Filter Test Doc"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long residentDocId = objectMapper.readTree(body).get("documentId").asLong();

        mockMvc.perform(get("/api/documents/resident/{residentId}", EXISTING_RESIDENT_ID)
                        .param("userId", String.valueOf(ADMIN_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(List.class)))
                .andExpect(jsonPath("$[*].residentId", everyItem(is(EXISTING_RESIDENT_ID.intValue()))))
                .andExpect(jsonPath("$[*].document").doesNotExist())
                .andExpect(jsonPath("$[*].id", hasItem((int) residentDocId)));

        // cleanup
        documentsRepository.findById(residentDocId).ifPresent(documentsRepository::delete);
    }
}