package com.smartbank.account.controller;
import com.smartbank.account.dto.AccountResponse;
import com.smartbank.account.dto.AmountRequest;
import com.smartbank.account.dto.CreateAccountRequest;
import com.smartbank.account.entity.Transaction;
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
    public ResponseEntity<AccountResponse> create(
            @Valid @RequestBody CreateAccountRequest request) {
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
    @PostMapping("/{id}/deposit")
    public ResponseEntity<AccountResponse> deposit(
            @PathVariable UUID id,
            @Valid @RequestBody AmountRequest request) {
        AccountResponse response = service.deposit(
                id, request.getAmount(), request.getDescription());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/{id}/withdraw")
    public ResponseEntity<AccountResponse> withdraw(
            @PathVariable UUID id,
            @Valid @RequestBody AmountRequest request) {
        AccountResponse response = service.withdraw(
                id, request.getAmount(), request.getDescription());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getTransactions(id));
    }
}