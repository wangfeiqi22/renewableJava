package com.renewable.ai.service;

import com.renewable.ai.entity.OrderPhoto;
import com.renewable.ai.repository.OrderPhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class FileStorageService {

    private final Path rootLocation = Paths.get("uploads");

    @Autowired
    private OrderPhotoRepository orderPhotoRepository;

    public FileStorageService() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public OrderPhoto storeFile(MultipartFile file, Long orderId, String nodeType) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file.");
            }
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path destinationFile = this.rootLocation.resolve(filename);
            Files.copy(file.getInputStream(), destinationFile);
            
            // Calculate File Hash for Integrity Check
            String fileHash = calculateFileHash(destinationFile);
            
            OrderPhoto photo = new OrderPhoto();
            photo.setOrderId(orderId);
            photo.setNodeType(nodeType);
            photo.setFileUrl("/uploads/" + filename);
            photo.setIsWatermarked(true); // Frontend sends watermarked file
            
            // Store hash in exif_data or a dedicated field if schema allows
            // Using a simple JSON structure for exif_data to store integrity info
            String integrityJson = String.format("{\"hash\": \"%s\", \"algorithm\": \"SHA-256\"}", fileHash);
            photo.setExifData(integrityJson); 
            
            return orderPhotoRepository.save(photo);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    private String calculateFileHash(Path path) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (var fis = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        return Base64.getEncoder().encodeToString(digest.digest());
    }
}
