package com.smartbank.fraud.controller;
import com.smartbank.fraud.entity.FraudAlert;
import com.smartbank.fraud.repository.FraudAlertRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.smartbank.fraud.ai.FraudAssistant;
import java.util.Map;
import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/fraud")
public class FraudController {
    private final FraudAlertRepository repository;
    private final FraudAssistant assistant;

    public FraudController(FraudAlertRepository repository,
                           FraudAssistant assistant) {
        this.repository = repository;
        this.assistant = assistant;
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
    public record AskRequest(String question) {}

    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> ask(@RequestBody AskRequest request) {
        if (request.question() == null || request.question().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Question is required"));
        }

        String answer = assistant.ask(request.question());
        return ResponseEntity.ok(Map.of(
                "question", request.question(),
                "answer", answer
        ));
    }
}