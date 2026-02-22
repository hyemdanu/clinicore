package com.clinicore.project.service;

import com.clinicore.project.entity.*;
import com.clinicore.project.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;
import java.util.Base64;

@Service
public class DocumentService {

    private final DocumentsRepository documentsRepository;
    private final UserProfileRepository userProfileRepository;

    public DocumentService(DocumentsRepository documentsRepository,
                           UserProfileRepository userProfileRepository) {
        this.documentsRepository = documentsRepository;
        this.userProfileRepository = userProfileRepository;
    }

    // Upload document
    public Map<String, Object> uploadDocument(Long currentUserId,
                                              Long residentId,
                                              String title,
                                              String type,
                                              MultipartFile file) throws IOException {

        UserProfile currentUser = userProfileRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + currentUserId));

        if (currentUser.getRole() == UserProfile.Role.RESIDENT &&
                !currentUser.getId().equals(residentId)) {
            throw new IllegalArgumentException("You do not have permission to upload documents");
        }

        Document document = new Document();
        document.setResidentId(residentId);
        document.setTitle(title);
        document.setType(type);
        document.setDocument(file.getBytes());
        documentsRepository.save(document);

        return Map.of(
                "message", "Document uploaded successfully",
                "documentId", document.getId()
        );
    }

    // Get ALL documents for a resident (or all if admin/caregiver)
    public List<Map<String, Object>> getDocuments(Long userId) {
        UserProfile currentUser = userProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        List<Document> documents;
        if (currentUser.getRole() == UserProfile.Role.ADMIN || currentUser.getRole() == UserProfile.Role.CAREGIVER) {
            documents = documentsRepository.findAll();
        } else {
            documents = documentsRepository.findByResidentId(currentUser.getId());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Document doc : documents) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", doc.getId());
            map.put("title", doc.getTitle());
            map.put("type", doc.getType());
            map.put("residentId", doc.getResidentId());
            map.put("uploaded_at", doc.getUploaded_at());
            result.add(map);
        }
        return result;
    }

    // Get Single Document (by ID)
    public Map<String, Object> getDocumentById(Long userId, Long documentId) {
        UserProfile currentUser = userProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        Document document = documentsRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        // Role validation
        if (currentUser.getRole() == UserProfile.Role.RESIDENT &&
                !document.getResidentId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You cannot view this document");
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", document.getId());
        map.put("title", document.getTitle());
        map.put("type", document.getType());
        map.put("residentId", document.getResidentId());
        map.put("uploaded_at", document.getUploaded_at());
        map.put("content", Base64.getEncoder().encodeToString(document.getDocument()));
        return map;
    }
}