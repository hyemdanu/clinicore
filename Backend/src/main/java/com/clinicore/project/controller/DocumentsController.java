package com.clinicore.project.controller;

import com.clinicore.project.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.clinicore.project.entity.Document;
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

    @GetMapping("/resident/{residentId}")
    public ResponseEntity<?> getDocumentsForResident(
            @PathVariable Long residentId,
            @RequestParam Long userId) {

        try {
            List<Map<String, Object>> docs =
                    documentService.getDocumentsForResident(userId, residentId);

            return ResponseEntity.ok(docs);

        } catch (IllegalArgumentException e) {
            // Permission or validation error → 403 or 400
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));

        } catch (Exception e) {
            // Real unexpected server error → 500
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Unexpected server error"));
        }
    }
    @GetMapping("/file/{documentId}")
    public ResponseEntity<byte[]> viewDocument(
            @PathVariable Long documentId,
            @RequestParam Long userId) {

        try {
            Document doc = documentService.getDocumentWithFile(documentId, userId);
            String type = doc.getType().toLowerCase();
            String title = doc.getTitle();

            String contentType;
            switch (type) {
                case "pdf": contentType = "application/pdf"; break;
                case "png": contentType = "image/png"; break;
                case "jpg":
                case "jpeg": contentType = "image/jpeg"; break;
                default: contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .header("Content-Disposition", "inline; filename=\"" + title + "\"")
                    .header("Content-Type", contentType)
                    .body(doc.getDocument());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<?> deleteDocument(
            @PathVariable Long documentId,
            @RequestParam Long userId) {

        try {
            documentService.deleteDocument(documentId, userId);
            return ResponseEntity.ok(Map.of("message", "Document deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error deleting document"));
        }
    }
}