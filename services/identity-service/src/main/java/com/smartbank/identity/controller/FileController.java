package com.smartbank.identity.controller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.nio.file.Path;
import java.nio.file.Paths;
@RestController
@RequestMapping("/files")
public class FileController {

    @Value("${storage.avatars.dir:/app/uploads/avatars}")
    private String avatarsDir;

    @GetMapping("/avatars/{filename}")
    public ResponseEntity<Resource> getAvatar(@PathVariable String filename) {
        try {
            Path base = Paths.get(avatarsDir).toAbsolutePath().normalize();
            Path file = base.resolve(filename).normalize();

            // Security: prevent path traversal
            if (!file.startsWith(base)) {
                return ResponseEntity.badRequest().build();
            }

            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = filename.endsWith(".png")  ? MediaType.IMAGE_PNG_VALUE
                    : filename.endsWith(".gif")  ? MediaType.IMAGE_GIF_VALUE
                    : "image/jpeg";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}