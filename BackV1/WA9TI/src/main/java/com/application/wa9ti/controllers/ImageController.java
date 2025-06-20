package com.application.wa9ti.controllers;


import com.application.wa9ti.services.auth.AuthorizationService;
import com.application.wa9ti.services.image.ImageUploadService;
import com.application.wa9ti.services.store.StoreServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private ImageUploadService imageUploadService;
    @Autowired
    private StoreServiceImp storeService;
    @Autowired
    private AuthorizationService authorizationService;

    /**
     * Upload an image for a specific store and owner.
     *
     * @param file The image file to upload.
     * @param ownerId The ID of the owner.
     * @param storeId The ID of the store.
     * @return The URL of the uploaded image.
     */
    @PostMapping("/upload-owner")
    public ResponseEntity<String> uploadOwnerImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("ownerId") Long ownerId,
            @RequestParam("storeId") Long storeId
    ) {
        authorizationService.canAccessStore(storeId);
        try {
            // Appel direct de la méthode unifiée dans le service
            String imageUrl = imageUploadService.uploadOwnerImage(file, ownerId);
            return ResponseEntity.ok(imageUrl);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading image.");
        }
    }


    /**
     * Upload an image for a specific store and owner.
     *
     * @param file The image file to upload.
     * @param storeId The ID of the store.
     * @return The URL of the uploaded image.
     */
    @PostMapping("/upload-store")
    public ResponseEntity<String> uploadStoreImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "storeId", required = false) Long storeId
    ) {
        authorizationService.canAccessStore(storeId);
        try {
            String imageUrl = imageUploadService.uploadStoreImage(file, storeId);
            return ResponseEntity.ok(imageUrl);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading image.");
        }
    }


    @PostMapping("/upload-employee")
    public ResponseEntity<String> uploadEmployeeImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("employeeId") Long employeeId) {
        try {
            // Appel direct de la méthode unifiée dans le service
            String imageUrl = imageUploadService.uploadEmployeeImage(file, employeeId);
            return ResponseEntity.ok(imageUrl);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading image.");
        }
    }

    @PostMapping("/upload-client")
    public ResponseEntity<String> uploadClientImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("clientId") Long clientId) {
        try {
            // Appel direct de la méthode unifiée dans le service
            String imageUrl = imageUploadService.uploadClientImage(file, clientId);
            return ResponseEntity.ok(imageUrl);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading image.");
        }
    }

    @GetMapping("/view/{ownerId}/{filename}")
    public ResponseEntity<byte[]> getImage(
            @PathVariable Long ownerId,
            @PathVariable String filename) {
        try {
            Path filePath = Paths.get(System.getProperty("user.dir") + "/uploads", String.valueOf(ownerId), filename);

            if (!Files.exists(filePath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            byte[] imageBytes = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType)) // ✅ Définit correctement le type
                    .body(imageBytes);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/view/{ownerId}/{storeId}/{filename}")
    public ResponseEntity<byte[]> getImage(
            @PathVariable Long ownerId,
            @PathVariable Long storeId,
            @PathVariable String filename) {
        try {
            Path filePath = Paths.get(System.getProperty("user.dir") + "/uploads", String.valueOf(ownerId), String.valueOf(storeId), filename);

            if (!Files.exists(filePath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            byte[] imageBytes = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType)) // ✅ Définit correctement le type
                    .body(imageBytes);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/view/{ownerId}/{storeId}/{employeeId}/{filename}")
    public ResponseEntity<byte[]> getImage(
            @PathVariable Long ownerId,
            @PathVariable Long storeId,
            @PathVariable Long employeeId,
            @PathVariable String filename) {
        try {
            Path filePath = Paths.get(System.getProperty("user.dir") + "/uploads", String.valueOf(ownerId), String.valueOf(storeId), String.valueOf(employeeId), filename);

            if (!Files.exists(filePath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            byte[] imageBytes = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType)) // ✅ Définit correctement le type
                    .body(imageBytes);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/view/{filename}")
    public ResponseEntity<byte[]> getImage(
            @PathVariable String filename) {
        try {
            Path filePath = Paths.get(System.getProperty("user.dir") + "/uploads",  filename);

            if (!Files.exists(filePath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            byte[] imageBytes = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType)) // ✅ Définit correctement le type
                    .body(imageBytes);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/view/clients/{clientId}/{filename}")
    public ResponseEntity<byte[]> getClientImage(
            @PathVariable Long clientId,
            @PathVariable String filename) {
        try {
            Path filePath = Paths.get(System.getProperty("user.dir") + "/uploads/clients", String.valueOf(clientId), filename);

            if (!Files.exists(filePath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            byte[] imageBytes = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType)) // ✅ Définit correctement le type
                    .body(imageBytes);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/view/employees/{employeeId}/{filename}")
    public ResponseEntity<byte[]> getEmployeeImage(
            @PathVariable Long employeeId,
            @PathVariable String filename) {
        try {
            Path filePath = Paths.get(System.getProperty("user.dir") + "/uploads/employees", String.valueOf(employeeId), filename);

            if (!Files.exists(filePath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            byte[] imageBytes = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType)) // ✅ Définit correctement le type
                    .body(imageBytes);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    /**
     * Health check endpoint to ensure the controller is working.
     *
     * @return A simple message.
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Image Controller is up and running!");
    }


    @PostMapping("/upload-multiple")
    public ResponseEntity<List<String>> uploadMultipleImages(
            @RequestParam("files[]") List<MultipartFile> files, // Correspond à la clé 'files[]' envoyée depuis Angular
            @RequestParam("storeId") Long storeId) {
        try {
            // Log pour vérifier les paramètres reçus
            System.err.println("Files received: "+ files.size());

            List<String> imageUrls = imageUploadService.uploadMultipleImages(files,storeId);
            return ResponseEntity.ok(imageUrls);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.singletonList(e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList("Error uploading images."));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteImage(@RequestBody Map<String, Object> payload) {
        try {
            String imageUrl = (String) payload.get("imageUrl");
            Long storeId = ((Number) payload.get("storeId")).longValue();

            // Supprime le fichier physique
            imageUploadService.deleteImage(imageUrl);

            // Supprime l'URL de l'image dans la base de données
            storeService.removeImageFromGallery(storeId, imageUrl);

            return ResponseEntity.ok("Image deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting image.");
        }
    }

}
