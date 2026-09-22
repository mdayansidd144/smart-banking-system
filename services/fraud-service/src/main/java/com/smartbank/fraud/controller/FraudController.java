package com.smartbank.fraud.controller;
import com.smartbank.fraud.entity.FraudAlert;
import com.smartbank.fraud.repository.FraudAlertRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/fraud")
public class FraudController {
    private final FraudAlertRepository repository;
    public FraudController(FraudAlertRepository repository) {
        this.repository = repository;
    }
    @GetMapping("/alerts")
    public ResponseEntity<List<FraudAlert>> getAllAlerts() {
        return ResponseEntity.ok(repository.findAllByOrderByCreatedAtDesc());
    }
    @GetMapping("/alerts/account/{accountId}")
    public ResponseEntity<List<FraudAlert>> getAlertsForAccount(@PathVariable UUID accountId) {
        return ResponseEntity.ok(repository.findByAccountIdOrderByCreatedAtDesc(accountId));
    }

    @GetMapping("/alerts/severity/{severity}")
    public ResponseEntity<List<FraudAlert>> getAlertsBySeverity(@PathVariable String severity) {
        return ResponseEntity.ok(
                repository.findBySeverityOrderByCreatedAtDesc(severity.toUpperCase())
        );
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Fraud service is running ");
    }
}