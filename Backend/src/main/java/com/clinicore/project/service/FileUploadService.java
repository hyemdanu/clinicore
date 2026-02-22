package com.clinicore.project.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * File Upload Service - handles file/image uploads for chat messages
 * stores files locally, returns the URL path for access
 */
@Service
public class FileUploadService {

    @Value("${file.upload-dir:uploads/messages}")
    private String uploadDir;

    // allowed image types
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    // allowed file types (documents)
    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain"
    );

    // create upload directory on startup if it doesn't exist
    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + e.getMessage());
        }
    }

    /**
     * upload a file and return the stored file info
     * returns: { url, originalName, type }
     */
    public FileUploadResult uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        String fileType = determineFileType(contentType);

        if (fileType == null) {
            throw new IllegalArgumentException("File type not allowed: " + contentType);
        }

        // generate unique filename to avoid collisions
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID() + extension;

        // save file to disk
        Path targetPath = Paths.get(uploadDir).resolve(newFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // return the URL path (relative to server root)
        String fileUrl = "/uploads/messages/" + newFilename;

        return new FileUploadResult(fileUrl, originalFilename, fileType);
    }

    /**
     * delete a file by its URL path
     */
    public boolean deleteFile(String fileUrl) {
        try {
            String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(uploadDir).resolve(filename);

            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * determine file content
     */
    private String determineFileType(String contentType) {
        if (contentType == null) {
            return null;
        }

        if (ALLOWED_IMAGE_TYPES.contains(contentType)) {
            return "IMAGE";
        }

        if (ALLOWED_FILE_TYPES.contains(contentType)) {
            return "FILE";
        }

        return null;
    }

    /**
     * get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * simple result class for upload response
     */
    public static class FileUploadResult {
        private final String url;
        private final String originalName;
        private final String type;

        public FileUploadResult(String url, String originalName, String type) {
            this.url = url;
            this.originalName = originalName;
            this.type = type;
        }

        public String getUrl() { return url; }
        public String getOriginalName() { return originalName; }
        public String getType() { return type; }
    }
}
