package com.smartbank.account.controller;

import com.smartbank.account.dto.AccountResponse;
import com.smartbank.account.dto.CreateAccountRequest;
import com.smartbank.account.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final AccountService service;
    public AccountController(AccountService service) {
        this.service = service;
    }
    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = service.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getAccount(id));
    }
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAll() {
        return ResponseEntity.ok(service.getAllAccounts());
    }
}