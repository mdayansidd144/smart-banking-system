package com.smartbank.notification.repository;
import com.smartbank.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findAllByOrderByCreatedAtDesc();
    List<Notification> findByStatusOrderByCreatedAtDesc(Notification.NotificationStatus status);
    List<Notification> findByEventTypeOrderByCreatedAtDesc(String eventType);
    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);
}