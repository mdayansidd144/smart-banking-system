package com.smartbank.notification;
import com.smartbank.notification.entity.Notification;
import com.smartbank.notification.repository.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationRepository repository;

    public NotificationController(NotificationRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getAll() {
        return ResponseEntity.ok(repository.findAllByOrderByCreatedAtDesc());
    }

    @GetMapping("/failed")
    public ResponseEntity<List<Notification>> getFailed() {
        return ResponseEntity.ok(
                repository.findByStatusOrderByCreatedAtDesc(Notification.NotificationStatus.FAILED)
        );
    }

    @GetMapping("/sent")
    public ResponseEntity<List<Notification>> getSent() {
        return ResponseEntity.ok(
                repository.findByStatusOrderByCreatedAtDesc(Notification.NotificationStatus.SENT)
        );
    }

    @GetMapping("/type/{eventType}")
    public ResponseEntity<List<Notification>> getByType(@PathVariable String eventType) {
        return ResponseEntity.ok(
                repository.findByEventTypeOrderByCreatedAtDesc(eventType.toUpperCase())
        );
    }
}