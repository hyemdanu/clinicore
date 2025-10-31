package com.clinicore.project.controller;

import com.clinicore.project.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    // Upload a document (only caregivers/admins)
    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(
            @RequestParam Long currentUserId,
            @RequestParam Long residentId,
            @RequestParam String title,
            @RequestParam String type,
            @RequestParam("file") MultipartFile file) {

        try {
            Map<String, Object> response = documentService.uploadDocument(currentUserId, residentId, title, type, file);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);
        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error uploading document: " + e.getMessage(), currentUserId);
        }
    }

    // Get all documents (role-based visibility)
    @GetMapping("/list")
    public ResponseEntity<?> getDocuments(@RequestParam Long currentUserId) {
        try {
            List<Map<String, Object>> documents = documentService.getDocuments(currentUserId);
            return ResponseEntity.ok(documents);
        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);
        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving documents: " + e.getMessage(), currentUserId);
        }
    }

    // Get a specific document by ID (role-based access)
    @GetMapping("/{documentId}")
    public ResponseEntity<?> getDocumentById(
            @PathVariable Long documentId,
            @RequestParam Long currentUserId) {

        try {
            Map<String, Object> document = documentService.getDocumentById(currentUserId, documentId);
            return ResponseEntity.ok(document);
        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);
        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving document: " + e.getMessage(), currentUserId);
        }
    }

    // Delete all documents for a resident (admin/caregiver only)
    @DeleteMapping("/delete/{residentId}")
    public ResponseEntity<?> deleteDocumentsByResident(
            @PathVariable Long residentId,
            @RequestParam Long currentUserId) {

        try {
            Map<String, Object> result = documentService.deleteDocumentsByResident(currentUserId, residentId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);
        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting documents: " + e.getMessage(), currentUserId);
        }
    }

    // Helper 4 error response
    private ResponseEntity<?> createErrorResponse(HttpStatus status, String message, Long userId) {
        return ResponseEntity.status(status).body(Map.of("message", message, "userId", userId));
    }
}