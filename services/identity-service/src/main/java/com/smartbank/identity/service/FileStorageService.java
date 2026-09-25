package com.smartbank.identity.service;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${storage.avatars.dir:/app/uploads/avatars}")
    private String avatarsDir;

    private Path avatarsPath;

    @PostConstruct
    public void init() {
        try {
            this.avatarsPath = Paths.get(avatarsDir).toAbsolutePath().normalize();
            Files.createDirectories(this.avatarsPath);
            log.info("Avatar storage initialized at {}", this.avatarsPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create avatar directory", e);
        }
    }

    /**
     * Save an uploaded avatar image.
     * Returns the URL path (relative) — e.g., /api/v1/files/avatars/<uuid>.jpg
     */
    public String saveAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed");
        }

        if (file.getSize() > 2 * 1024 * 1024) {  // 2 MB
            throw new RuntimeException("Image must be under 2 MB");
        }

        String ext = getExtension(file.getOriginalFilename(), contentType);
        String filename = UUID.randomUUID() + ext;

        try {
            Path target = avatarsPath.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            log.info("Saved avatar: {}", target);
            return "/api/v1/files/avatars/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save avatar", e);
        }
    }

    private String getExtension(String originalName, String contentType) {
        if (originalName != null && originalName.contains(".")) {
            String ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
            if (ext.length() <= 5) return ext;
        }
        if ("image/png".equals(contentType))  return ".png";
        if ("image/gif".equals(contentType))  return ".gif";
        if ("image/webp".equals(contentType)) return ".webp";
        return ".jpg";
    }
}