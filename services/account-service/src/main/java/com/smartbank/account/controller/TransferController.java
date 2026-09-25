package com.smartbank.account.controller;

import com.smartbank.account.audit.Auditable;
import com.smartbank.account.audit.AuditAction;
import com.smartbank.account.dto.TransferRequest;
import com.smartbank.account.dto.TransferResponse;
import com.smartbank.account.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService service;

    public TransferController(TransferService service) {
        this.service = service;
    }

    @PostMapping
    @Auditable(action = AuditAction.TRANSFER, resourceType = "TRANSFER")
    public ResponseEntity<TransferResponse> transfer(
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody TransferRequest request) {

        TransferResponse response = service.transfer(idempotencyKey, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/reverse")
    @Auditable(action = AuditAction.TRANSFER_REVERSE, resourceIdParam = "id", resourceType = "TRANSFER")
    public ResponseEntity<TransferResponse> reverse(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {

        String reason = (body != null && body.get("reason") != null)
                ? body.get("reason")
                : "Customer requested reversal";

        return ResponseEntity.ok(service.reverseTransfer(id, reason));
    }
}