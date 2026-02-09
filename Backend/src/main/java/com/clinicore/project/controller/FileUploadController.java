package com.clinicore.project.controller;

import com.clinicore.project.service.FileUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * File controller for images/files/docs
 */
@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    /**
     * upload a file/image here
     * it returns the file url and allow for us in the messages
     */
    @PostMapping("/chat")
    public ResponseEntity<?> uploadChatFile(@RequestParam("file") MultipartFile file) {
        try {
            FileUploadService.FileUploadResult result = fileUploadService.uploadFile(file);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "url", result.getUrl(),
                    "originalName", result.getOriginalName(),
                    "type", result.getType()
            ));
        } catch (IllegalArgumentException e) {
            // error checking if file upload is empty
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "error", "Upload failed: " + e.getMessage()));
        }
    }

    /**
     * delete an uploaded file
     */
    @DeleteMapping("/chat")
    public ResponseEntity<?> deleteChatFile(@RequestParam String fileUrl) {
        try {
            boolean deleted = fileUploadService.deleteFile(fileUrl);

            if (deleted) {
                return ResponseEntity.ok(Map.of("success", true));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "error", "Delete failed: " + e.getMessage()));
        }
    }
}
