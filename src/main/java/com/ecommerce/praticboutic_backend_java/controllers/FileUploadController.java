package com.ecommerce.praticboutic_backend_java.controllers;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
        import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api")
public class FileUploadController {

    @Value("${file.upload.directory:upload}")
    private String uploadDirectory;

    @Value("${file.upload.maxSize:5242880}")  // Default 5MB
    private long maxFileSize;

    private final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".png", ".gif", ".jpg", ".jpeg");
    private final List<String> ALLOWED_MIME_TYPES = Arrays.asList("image/gif", "image/png", "image/jpeg");

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam(value = "file", required = false) MultipartFile file,
                                        HttpSession session) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pas de fichier");
        }

        try {
            // Create directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDirectory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Get file details
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null ?
                    originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase() : "";
            String contentType = file.getContentType();
            long fileSize = file.getSize();

            // Validation checks
            if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error","Vous devez uploader un fichier de type png, gif, jpg, jpeg..."));
            }

            if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Type mime non reconnu"));
            }

            if (fileSize > maxFileSize) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Le fichier est trop gros..."));
            }

            // Generate unique filename
            String newFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(newFilename);

            // Save the file
            Files.copy(file.getInputStream(), filePath);

            // Store filename in session
            session.setAttribute("initboutic_logo", newFilename);

            return ResponseEntity.ok().body(Map.of("result", newFilename));

        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error","Echec de l'upload!"));
        }
    }

    @PostMapping("/boupload")
    public ResponseEntity<?> uploadFiles(@RequestParam(value = "file[]", required = false) MultipartFile[] files,
                                         @RequestParam(value = "sessionid", required = false) String sessionId,
                                         HttpSession session) {

        // Set session ID if provided
        /*if (sessionId != null && !sessionId.isEmpty()) {
            // Note: Direct session ID manipulation might require custom configuration in Spring
            // This is a placeholder for the PHP session_id() equivalent
        }*/

        if (files == null || files.length == 0) {
            return ResponseEntity.ok().body(new ArrayList<>());
        }

        List<String> uploadedFiles = new ArrayList<>();

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDirectory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Process each file
            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;

                // Get file details
                String originalFilename = file.getOriginalFilename();
                String fileExtension = originalFilename != null ?
                        originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase() : "";
                String contentType = file.getContentType();
                long fileSize = file.getSize();

                // Validation checks
                if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body("{\"error\": \"Vous devez uploader des fichiers de type png, gif, jpg, jpeg...\"}");
                }

                if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body("{\"error\": \"Un des type mime d'un fichier est non reconnu\"}");
                }

                if (fileSize > maxFileSize) {
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body("{\"error\": \"Un des fichiers est trop gros...\"}");
                }

                // Generate unique filename
                String newFilename = UUID.randomUUID().toString() + fileExtension;
                Path filePath = uploadPath.resolve(newFilename);

                // Save the file
                Files.copy(file.getInputStream(), filePath);

                // Add filename to the result list
                uploadedFiles.add(newFilename);
            }

            return ResponseEntity.ok().body(uploadedFiles);

        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Echec d'un upload de fichier !\"}");
        }
    }
}