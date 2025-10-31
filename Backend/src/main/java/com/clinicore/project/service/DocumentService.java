package com.clinicore.project.service;

import com.clinicore.project.entity.*;
import com.clinicore.project.repository.*;
import org.springframework.stereotype.Service;
import java.util.*;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {

    private final DocumentsRepository documentsRepository;
    private final UserProfileRepository userProfileRepository;

    public DocumentService(DocumentsRepository documentsRepository,
                           UserProfileRepository userProfileRepository) {
        this.documentsRepository = documentsRepository;
        this.userProfileRepository = userProfileRepository;
    }

    //Uploading Document (Only by caregiver/admin)
    public Map<String, Object> uploadDocument(Long currentUserId, Long residentId, String title, String type, MultipartFile file) throws IOException {

        UserProfile currentUser = getUserById(currentUserId);

        //if caregiver/admin
        if (currentUser.getRole() != UserProfile.Role.CAREGIVER &&
                currentUser.getRole() != UserProfile.Role.ADMIN) {
            throw new IllegalArgumentException("You do not have permission to upload documents");
        }

        Document document = new Document();
        document.setResidentId(residentId);
        document.setTitle(title);
        document.setType(type);
        document.setDocument(file.getBytes());
        document.setUploaded_at(new Date());
        documentsRepository.save(document);

        return Map.of(
                "message", "Document uploaded successfully",
                "documentId", document.getId()
        );
    }

    //Get ALL documents (if caregiver/admin) Get OWN documents (residents)
    UserProfile currentUser = getUserById(currentUserId);
    List<Document> documents;

    // Admins and caregivers see all
    if (currentUser.getRole() == UserProfile.Role.ADMIN ||
            currentUser.getRole() == UserProfile.Role.CAREGIVER) {
        documents = documentsRepository.findAll();
    }
    // Residents see only their own
        else if (currentUser.getRole() == UserProfile.Role.RESIDENT) {
        documents = documentsRepository.findByResidentId(currentUser.getId());
    }
    // Everyone else — denied !!!!
        else {
        throw new IllegalArgumentException("You do not have permission to view documents");
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

    //Get Single Document (by ID)
    public Map<String, Object> getDocumentById(Long currentUserId, Long documentId) {
        UserProfile currentUser = getUserById(currentUserId);
        Document document = documentsRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        //Role validation
        if (currentUser.getRole() == UserProfile.Role.ADMIN ||
                currentUser.getRole() == UserProfile.Role.CAREGIVER ||
                (currentUser.getRole() == UserProfile.Role.RESIDENT &&
                        document.getResidentId().equals(currentUser.getId())))



    }