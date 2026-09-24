package com.smartbank.fraud.controller;
import com.smartbank.fraud.entity.AnomalyAlert;
import com.smartbank.fraud.entity.AnomalySeverity;
import com.smartbank.fraud.repository.AnomalyAlertRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/anomalies")
public class AnomalyController {

    private final AnomalyAlertRepository repository;

    public AnomalyController(AnomalyAlertRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<AnomalyAlert>> listAll() {
        return ResponseEntity.ok(repository.findAllByOrderByCreatedAtDesc());
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<AnomalyAlert>> listByAccount(@PathVariable UUID accountId) {
        return ResponseEntity.ok(repository.findByAccountIdOrderByCreatedAtDesc(accountId));
    }

    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<AnomalyAlert>> listBySeverity(
            @PathVariable AnomalySeverity severity) {
        return ResponseEntity.ok(repository.findBySeverityOrderByCreatedAtDesc(severity));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<AnomalyAlert>> listByStatus(
            @PathVariable AnomalyAlert.AlertStatus status) {
        return ResponseEntity.ok(repository.findByStatusOrderByCreatedAtDesc(status));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AnomalyAlert> updateStatus(
            @PathVariable UUID id,
            @RequestParam AnomalyAlert.AlertStatus status) {

        AnomalyAlert alert = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Anomaly not found: " + id));
        alert.setStatus(status);
        return ResponseEntity.ok(repository.save(alert));
    }
}