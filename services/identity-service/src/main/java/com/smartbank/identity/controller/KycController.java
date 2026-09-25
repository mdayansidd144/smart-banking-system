package com.smartbank.identity.controller;

import com.smartbank.identity.dto.KycRequest;
import com.smartbank.identity.dto.KycResponse;
import com.smartbank.identity.service.KycService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/kyc")
public class KycController {

    private final KycService service;

    public KycController(KycService service) {
        this.service = service;
    }

    // =========================================================
    // USER endpoints
    // =========================================================

    @PostMapping("/submit")
    public ResponseEntity<KycResponse> submit(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody KycRequest request) {

        UUID userId = extractUserId(authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.submit(userId, request));
    }

    @GetMapping("/status")
    public ResponseEntity<KycResponse> status(@RequestHeader("Authorization") String authHeader) {
        UUID userId = extractUserId(authHeader);
        return ResponseEntity.ok(service.getForUser(userId));
    }

    // =========================================================
    // ADMIN endpoints
    // =========================================================

    @GetMapping("/admin/pending")
    public ResponseEntity<List<KycResponse>> listPending(
            @RequestHeader("Authorization") String authHeader) {
        requireAdmin(authHeader);
        return ResponseEntity.ok(service.listPending());
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<KycResponse>> listAll(
            @RequestHeader("Authorization") String authHeader) {
        requireAdmin(authHeader);
        return ResponseEntity.ok(service.listAll());
    }

    @PostMapping("/admin/{id}/approve")
    public ResponseEntity<KycResponse> approve(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        UUID adminId = extractUserId(authHeader);
        requireAdmin(authHeader);
        return ResponseEntity.ok(service.approve(id, adminId));
    }

    @PostMapping("/admin/{id}/reject")
    public ResponseEntity<KycResponse> reject(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) Map<String, String> body) {
        UUID adminId = extractUserId(authHeader);
        requireAdmin(authHeader);
        String reason = (body != null && body.get("reason") != null)
                ? body.get("reason")
                : "No reason provided";
        return ResponseEntity.ok(service.reject(id, adminId, reason));
    }

    // =========================================================
    // JWT helpers (base64-decode payload — no verification needed)
    // =========================================================

    private UUID extractUserId(String authHeader) {
        String payload = extractJwtPayload(authHeader);
        String userId = readJsonField(payload, "userId");
        if (userId == null || userId.isBlank()) {
            throw new RuntimeException("userId not found in token");
        }
        return UUID.fromString(userId);
    }

    private void requireAdmin(String authHeader) {
        String payload = extractJwtPayload(authHeader);
        String role = readJsonField(payload, "role");
        if (role == null || !"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Admin access required");
        }
    }

    private String extractJwtPayload(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing Authorization header");
        }
        String token = authHeader.substring(7).trim();
        String[] parts = token.split("\\.");
        if (parts.length < 2) throw new RuntimeException("Invalid token");
        try {
            return new String(Base64.getUrlDecoder().decode(parts[1]));
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse token");
        }
    }

    private String readJsonField(String json, String field) {
        int idx = json.indexOf("\"" + field + "\"");
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx);
        if (colon < 0) return null;
        int start = json.indexOf('"', colon + 1);
        if (start < 0) return null;
        int end = json.indexOf('"', start + 1);
        if (end < 0) return null;
        return json.substring(start + 1, end);
    }

    // =========================================================
    // Exception handler
    // =========================================================

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleError(RuntimeException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Unknown error";
        HttpStatus status = msg.contains("Admin")
                ? HttpStatus.FORBIDDEN
                : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(Map.of("error", msg));
    }
}