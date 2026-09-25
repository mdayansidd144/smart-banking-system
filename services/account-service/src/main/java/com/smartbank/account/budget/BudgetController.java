package com.smartbank.account.budget;
import com.smartbank.account.audit.AuditAction;
import com.smartbank.account.audit.Auditable;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/budgets")
public class BudgetController {

    private final BudgetService service;

    public BudgetController(BudgetService service) {
        this.service = service;
    }

    /**
     * Extract userId from the JWT in the Authorization header.
     * (Same pattern as AuditAspect — base64-decode payload, no verification needed
     * because the API Gateway already verified the token.)
     */
    private String extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing Authorization header");
        }
        String token = authHeader.substring(7);
        String[] parts = token.split("\\.");
        if (parts.length < 2) throw new RuntimeException("Invalid token");

        try {
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            // Cheap parse: extract "userId":"..."
            int idx = payload.indexOf("\"userId\"");
            if (idx < 0) return "anonymous";

            int colon = payload.indexOf(':', idx);
            int start = payload.indexOf('"', colon + 1);
            int end = payload.indexOf('"', start + 1);
            return payload.substring(start + 1, end);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse token");
        }
    }

    // =========================================================
    // CRUD
    // =========================================================

    @PostMapping
    @Auditable(action = AuditAction.UPDATE_ACCOUNT, resourceType = "BUDGET")
    public ResponseEntity<BudgetResponse> create(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody BudgetRequest request) {

        String userId = extractUserId(authHeader);
        BudgetResponse response = service.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> list(
            @RequestHeader("Authorization") String authHeader) {

        String userId = extractUserId(authHeader);
        return ResponseEntity.ok(service.listForUser(userId));
    }

    @PatchMapping("/{id}")
    @Auditable(action = AuditAction.UPDATE_ACCOUNT, resourceIdParam = "id", resourceType = "BUDGET")
    public ResponseEntity<BudgetResponse> update(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody BudgetRequest request) {

        String userId = extractUserId(authHeader);
        return ResponseEntity.ok(service.update(id, userId, request));
    }

    @DeleteMapping("/{id}")
    @Auditable(action = AuditAction.UPDATE_ACCOUNT, resourceIdParam = "id", resourceType = "BUDGET")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {

        String userId = extractUserId(authHeader);
        service.delete(id, userId);
        return ResponseEntity.ok(Map.of("message", "Budget deleted"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleError(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}