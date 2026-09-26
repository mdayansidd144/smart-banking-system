package com.smartbank.fraud.controller;
import com.smartbank.fraud.dto.AmortizationSchedule;
import com.smartbank.fraud.dto.LoanApplicationRequest;
import com.smartbank.fraud.dto.LoanApplicationResponse;
import com.smartbank.fraud.service.AmortizationService;
import com.smartbank.fraud.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/loans")
public class LoanController {

    private final LoanService service;
    private final AmortizationService amortizationService;

    public LoanController(LoanService service,
                          AmortizationService amortizationService) {
        this.service = service;
        this.amortizationService = amortizationService;
    }

    @PostMapping("/apply")
    public ResponseEntity<LoanApplicationResponse> apply(
            @Valid @RequestBody LoanApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.apply(request));
    }

    @GetMapping
    public ResponseEntity<List<LoanApplicationResponse>> listAll() {
        return ResponseEntity.ok(service.listAll());
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<LoanApplicationResponse>> listByAccount(
            @PathVariable UUID accountId) {
        return ResponseEntity.ok(service.listByAccount(accountId));
    }
    @GetMapping("/{id}/amortization")
    public ResponseEntity<AmortizationSchedule> getAmortization(
            @PathVariable UUID id) {
        return ResponseEntity.ok(amortizationService.generate(id));
    }
}