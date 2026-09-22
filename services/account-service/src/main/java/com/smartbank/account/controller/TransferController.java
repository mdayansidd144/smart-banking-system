package com.smartbank.account.controller;
import com.smartbank.account.dto.TransferRequest;
import com.smartbank.account.dto.TransferResponse;
import com.smartbank.account.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService service;

    public TransferController(TransferService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TransferResponse> transfer(
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody TransferRequest request) {

        TransferResponse response = service.transfer(idempotencyKey, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}