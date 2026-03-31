package com.clinicore.project.service;

import com.clinicore.project.entity.*;
import com.clinicore.project.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;
import java.util.Base64;

@Service
public class DocumentService {

    private final DocumentsRepository documentsRepository;
    private final UserProfileRepository userProfileRepository;
    private final ResidentCaregiverRepository residentCaregiverRepository;
    private final HashService_SHA256 hashService;

    public DocumentService(DocumentsRepository documentsRepository,
                           UserProfileRepository userProfileRepository,
                           ResidentCaregiverRepository residentCaregiverRepository,
                           HashService_SHA256 hashService) {
        this.documentsRepository = documentsRepository;
        this.userProfileRepository = userProfileRepository;
        this.residentCaregiverRepository = residentCaregiverRepository;
        this.hashService = hashService;
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
        byte[] fileBytes = file.getBytes();
        String hash = hashService.hashBytes(fileBytes);
        document.setDocument(fileBytes);
        document.setFileHash(hash);
        documentsRepository.save(document);

        return Map.of(
                "message", "Document uploaded successfully",
                "documentId", document.getId()
        );
    }

    // Get ALL documents for a resident (or all if admin/caregiver)
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDocuments(Long userId) {
        UserProfile currentUser = userProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        List<DocumentsRepository.DocumentMetadata> documents;
        if (currentUser.getRole() == UserProfile.Role.ADMIN || currentUser.getRole() == UserProfile.Role.CAREGIVER) {
            documents = documentsRepository.findAllMetadata();
        } else {
            documents = documentsRepository.findMetadataByResidentId(currentUser.getId());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (DocumentsRepository.DocumentMetadata doc : documents) {
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
    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDocumentsForResident(Long userId, Long residentId) {

        UserProfile currentUser = userProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (currentUser.getRole() == UserProfile.Role.CAREGIVER) {
            boolean assigned = residentCaregiverRepository
                    .existsByIdCaregiverIdAndIdResidentId(userId, residentId);
            if (!assigned) {
                throw new IllegalArgumentException("You are not assigned to this resident");
            }
        } else if (currentUser.getRole() == UserProfile.Role.RESIDENT) {
            if (!currentUser.getId().equals(residentId)) {
                throw new IllegalArgumentException("You cannot view this resident's documents");
            }
        } else if (currentUser.getRole() != UserProfile.Role.ADMIN) {
            throw new IllegalArgumentException("Invalid role");
        }

        List<DocumentsRepository.DocumentMetadata> docs = documentsRepository.findMetadataByResidentId(residentId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (DocumentsRepository.DocumentMetadata doc : docs) {
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

    @Transactional(readOnly = true)
    public Document getDocumentWithFile(Long documentId, Long userId) {
        UserProfile currentUser = userProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Document document = documentsRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        if (currentUser.getRole() == UserProfile.Role.RESIDENT &&
                !document.getResidentId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You cannot view this document");
        } else if (currentUser.getRole() == UserProfile.Role.CAREGIVER) {
            boolean assigned = residentCaregiverRepository
                    .existsByIdCaregiverIdAndIdResidentId(userId, document.getResidentId());
            if (!assigned) throw new IllegalArgumentException("You are not assigned to this resident");
        }

        return document;
    }
}