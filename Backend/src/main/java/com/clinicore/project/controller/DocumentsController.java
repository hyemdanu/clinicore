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
public class DocumentsController {

    private final DocumentService documentService;

    public DocumentsController(DocumentService documentService) {
        this.documentService = documentService;
    }

    // Upload a document
    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(
            @RequestParam Long currentUserId,
            @RequestParam Long residentId,
            @RequestParam String title,
            @RequestParam String type,
            @RequestParam("file") MultipartFile file) {

        try {
            Map<String, Object> response =
                    documentService.uploadDocument(currentUserId, residentId, title, type, file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // Get all documents (role-based visibility)
    @GetMapping("/list")
    public ResponseEntity<?> getDocuments(@RequestParam Long userId) {
        try {
            List<Map<String, Object>> documents = documentService.getDocuments(userId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error fetching documents: " + e.getMessage()));
        }
    }

    // Get a specific document by ID
    @GetMapping("/{documentId}")
    public ResponseEntity<?> getDocumentById(@RequestParam Long userId,
                                             @PathVariable Long documentId) {
        try {
            Map<String, Object> doc = documentService.getDocumentById(userId, documentId);
            return ResponseEntity.ok(doc);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error fetching document: " + e.getMessage()));
        }
    }
}