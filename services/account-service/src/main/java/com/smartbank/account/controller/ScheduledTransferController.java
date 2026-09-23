package com.smartbank.account.controller;
import com.smartbank.account.dto.CreateScheduledTransferRequest;
import com.smartbank.account.dto.ScheduledTransferResponse;
import com.smartbank.account.service.ScheduledTransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/scheduled-transfers")
public class ScheduledTransferController {

    private final ScheduledTransferService service;

    public ScheduledTransferController(ScheduledTransferService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ScheduledTransferResponse> create(
            @Valid @RequestBody CreateScheduledTransferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    public ResponseEntity<List<ScheduledTransferResponse>> listAll() {
        return ResponseEntity.ok(service.listAll());
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<ScheduledTransferResponse>> listByAccount(
            @PathVariable UUID accountId) {
        return ResponseEntity.ok(service.listByAccount(accountId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ScheduledTransferResponse> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(service.cancel(id));
    }
}