package com.application.wa9ti.services.image;

import com.application.wa9ti.services.client.ClientServiceImp;
import com.application.wa9ti.services.employee.ImpEmployeeService;
import com.application.wa9ti.services.owner.OwnerServiceImp;
import com.application.wa9ti.services.store.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ImageUploadService {

    @Autowired
    private StoreService storeService;

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads";
    @Autowired
    private ImpEmployeeService impEmployeeService;
    @Autowired
    private OwnerServiceImp ownerServiceImp;
    @Autowired
    private ClientServiceImp clientServiceImp;

    // Racine des uploads

    /**
     * Upload an image for a specific store under a specific owner.
     *
     * @param file The image file to upload.
     * @param ownerId The ID of the owner.
     * @return The URL of the uploaded image.
     * @throws IOException If an error occurs during file upload.
     */
    public String uploadOwnerImage(MultipartFile file, Long ownerId) throws IOException {
        // Validez le type de fichier
        if (file.isEmpty() || !isImageFile(file)) {
            throw new IllegalArgumentException("Invalid image file.");
        }

        // Définissez la structure de répertoire en fonction des paramètres
        Path targetDir;
        targetDir = Paths.get(UPLOAD_DIR, String.valueOf(ownerId));

        // Assurez-vous que le répertoire existe
        if (!Files.exists(targetDir)) {
            try {
                Files.createDirectories(targetDir);
                System.out.println("Directory created: " + targetDir.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("Failed to create directory: " + targetDir.toAbsolutePath());
                throw e;
            }
        }

        // Générer un nom de fichier unique
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = targetDir.resolve(filename);

        // Sauvegardez le fichier
        try {
            Files.copy(file.getInputStream(), filePath);
            System.out.println("File saved to: " + filePath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save file: " + filePath.toAbsolutePath());
            throw e;
        }

        // Retournez l'URL relative pour l'accès
        String imageUrl = ownerId + "/" + filename;
        ownerServiceImp.updateOwnerImage(ownerId, imageUrl);
        return imageUrl;
    }

    /**
     * Upload an image for a specific store under a specific owner.
     *
     * @param file The image file to upload.
     * @param storeId The ID of the store.
     * @return The URL of the uploaded image.
     * @throws IOException If an error occurs during file upload.
     */
    public String uploadStoreImage(MultipartFile file, Long storeId) throws IOException {
        // Validez le type de fichier
        if (file.isEmpty() || !isImageFile(file)) {
            throw new IllegalArgumentException("Invalid image file.");
        }

        // Définissez la structure de répertoire en fonction des paramètres
        Path targetDir;
        targetDir = Paths.get(UPLOAD_DIR, String.valueOf(storeId));


        // Assurez-vous que le répertoire existe
        if (!Files.exists(targetDir)) {
            try {
                Files.createDirectories(targetDir);
                System.out.println("Directory created: " + targetDir.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("Failed to create directory: " + targetDir.toAbsolutePath());
                throw e;
            }
        }

        // Générer un nom de fichier unique
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = targetDir.resolve(filename);

        // Sauvegardez le fichier
        try {
            Files.copy(file.getInputStream(), filePath);
            System.out.println("File saved to: " + filePath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save file: " + filePath.toAbsolutePath());
            throw e;
        }

        // Retournez l'URL relative pour l'accès
        String imageUrl = storeId+ "/" + filename;

        storeService.updateStoreImage(storeId, imageUrl);

        return imageUrl;
    }

    public String uploadEmployeeImage(MultipartFile file,Long employeeId) throws IOException {
        // Validez le type de fichier
        if (file.isEmpty() || !isImageFile(file)) {
            throw new IllegalArgumentException("Invalid image file.");
        }

        // Cas : upload d'image pour un propriétaire
        Path targetDir = Paths.get(UPLOAD_DIR, "employees",String.valueOf(employeeId));

        // Assurez-vous que le répertoire existe
        if (!Files.exists(targetDir)) {
            try {
                Files.createDirectories(targetDir);
                System.out.println("Directory created: " + targetDir.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("Failed to create directory: " + targetDir.toAbsolutePath());
                throw e;
            }
        }

        // Générer un nom de fichier unique
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = targetDir.resolve(filename);

        // Sauvegardez le fichier
        try {
            Files.copy(file.getInputStream(), filePath);
            System.out.println("File saved to: " + filePath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save file: " + filePath.toAbsolutePath());
            throw e;
        }

        // Retournez l'URL relative pour l'accès
        String imageUrl = "employees/"+employeeId +  "/" + filename;

        impEmployeeService.updateEmployeeImage(employeeId, imageUrl);

        return imageUrl;
    }

    public String uploadClientImage(MultipartFile file,Long clientId) throws IOException {
        // Validez le type de fichier
        if (file.isEmpty() || !isImageFile(file)) {
            throw new IllegalArgumentException("Invalid image file.");
        }

        // Cas : upload d'image pour un propriétaire
        Path targetDir = Paths.get(UPLOAD_DIR, "clients",String.valueOf(clientId));

        // Assurez-vous que le répertoire existe
        if (!Files.exists(targetDir)) {
            try {
                Files.createDirectories(targetDir);
                System.out.println("Directory created: " + targetDir.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("Failed to create directory: " + targetDir.toAbsolutePath());
                throw e;
            }
        }

        // Générer un nom de fichier unique
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = targetDir.resolve(filename);

        // Sauvegardez le fichier
        try {
            Files.copy(file.getInputStream(), filePath);
            System.out.println("File saved to: " + filePath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save file: " + filePath.toAbsolutePath());
            throw e;
        }

        // Retournez l'URL relative pour l'accès
        String imageUrl = "clients/"+clientId +  "/" + filename;

        clientServiceImp.updateClientImage(clientId, imageUrl);

        return imageUrl;
    }



    /**
     * Check if the file is an image based on its content type.
     *
     * @param file The file to check.
     * @return True if the file is an image, otherwise false.
     */
    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }


    /**
     * Upload multiple images for a specific store.
     *
     * @param files   The list of image files to upload.
     * @param storeId The ID of the store.
     * @return A list of URLs of the uploaded images.
     * @throws IOException If an error occurs during file upload.
     */
    public List<String> uploadMultipleImages(List<MultipartFile> files,Long storeId) throws IOException {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("No files provided for upload.");
        }

        List<String> imageUrls = new ArrayList<>();

        Path targetDir = Paths.get(UPLOAD_DIR, String.valueOf(storeId));

        // Ensure the directory exists
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        for (MultipartFile file : files) {
            if (file.isEmpty() || !isImageFile(file)) {
                throw new IllegalArgumentException("Invalid image file: " + file.getOriginalFilename());
            }

            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = targetDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            String imageUrl = storeId + "/" + filename;
            imageUrls.add(imageUrl);
        }
        storeService.addImageToGallery(storeId,imageUrls);
        return imageUrls;
    }

    public void deleteImage(String imageUrl) throws IOException {
        Path filePath = Paths.get(UPLOAD_DIR, imageUrl);

        if (Files.exists(filePath)) {
            Files.delete(filePath);
            System.out.println("Image deleted: " + filePath.toAbsolutePath());
        } else {
            throw new IllegalArgumentException("Image not found at " + filePath.toAbsolutePath());
        }
    }
}
