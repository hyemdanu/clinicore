package com.clinicore.project.controller;

import com.clinicore.project.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.clinicore.project.entity.Document;
import java.util.*;
import java.util.Set;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin
public class DocumentsController {

    private final DocumentService documentService;

    public DocumentsController(DocumentService documentService) {
        this.documentService = documentService;
    }

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "png", "jpg", "jpeg", "docx", "xlsx");

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(
            @RequestParam Long currentUserId,
            @RequestParam Long residentId,
            @RequestParam String title,
            @RequestParam("file") MultipartFile file) {

        if (title == null || title.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Title is required"));
        }
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "File is required"));
        }

        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
        }
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "File type not allowed. Accepted: PDF, PNG, JPG, DOCX, XLSX"));
        }

        try {
            Map<String, Object> response =
                    documentService.uploadDocument(currentUserId, residentId, title.trim(), extension, file);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
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
            byte[] data = doc.getDocument();
            String title = doc.getTitle();

            // detect content type from file magic bytes
            String contentType = "application/octet-stream";
            String storedType = doc.getType();
            if (data != null && data.length >= 4) {
                if (data[0] == 0x25 && data[1] == 0x50 && data[2] == 0x44 && data[3] == 0x46) {
                    contentType = "application/pdf";
                } else if ((data[0] & 0xFF) == 0x89 && data[1] == 0x50 && data[2] == 0x4E && data[3] == 0x47) {
                    contentType = "image/png";
                } else if ((data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8 && (data[2] & 0xFF) == 0xFF) {
                    contentType = "image/jpeg";
                } else if (data[0] == 0x50 && data[1] == 0x4B) {
                    // ZIP signature — differentiate docx vs xlsx using stored type
                    if ("xlsx".equals(storedType)) {
                        contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                    } else {
                        contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                    }
                }
            }

            return ResponseEntity.ok()
                    .header("Content-Disposition", "inline; filename=\"" + title + "\"")
                    .header("Content-Type", contentType)
                    .body(data);

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