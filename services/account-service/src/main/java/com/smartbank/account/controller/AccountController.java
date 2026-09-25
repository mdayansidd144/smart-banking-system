package com.smartbank.account.controller;
import com.smartbank.account.audit.Auditable;
import com.smartbank.account.audit.AuditAction;
import com.smartbank.account.dto.AccountResponse;
import com.smartbank.account.dto.AmountRequest;
import com.smartbank.account.dto.CreateAccountRequest;
import com.smartbank.account.entity.Account;
import com.smartbank.account.entity.Transaction;
import com.smartbank.account.event.EventPublisher;
import com.smartbank.account.event.StatementRequestedEvent;
import com.smartbank.account.repository.AccountRepository;
import com.smartbank.account.service.AccountService;
import com.smartbank.account.service.StatementService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService service;
    private final StatementService statementService;
    private final EventPublisher eventPublisher;
    private final AccountRepository accountRepository;

    public AccountController(AccountService service,
                             StatementService statementService,
                             EventPublisher eventPublisher,
                             AccountRepository accountRepository) {
        this.service = service;
        this.statementService = statementService;
        this.eventPublisher = eventPublisher;
        this.accountRepository = accountRepository;
    }

    @PostMapping
    @Auditable(action = AuditAction.CREATE_ACCOUNT, resourceType = "ACCOUNT")
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
    @Auditable(action = AuditAction.DEPOSIT, accountIdParam = "id", resourceType = "ACCOUNT")
    public ResponseEntity<AccountResponse> deposit(
            @PathVariable UUID id,
            @Valid @RequestBody AmountRequest request) {
        return ResponseEntity.ok(service.deposit(id, request.getAmount(), request.getDescription()));
    }

    @PostMapping("/{id}/withdraw")
    @Auditable(action = AuditAction.WITHDRAWAL, accountIdParam = "id", resourceType = "ACCOUNT")
    public ResponseEntity<AccountResponse> withdraw(
            @PathVariable UUID id,
            @Valid @RequestBody AmountRequest request) {
        return ResponseEntity.ok(service.withdraw(id, request.getAmount(), request.getDescription()));
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getTransactions(id));
    }

    @GetMapping("/{id}/statement")
    @Auditable(action = AuditAction.DOWNLOAD_STATEMENT, accountIdParam = "id", resourceType = "ACCOUNT")
    public ResponseEntity<byte[]> getStatement(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        byte[] pdf = statementService.generateStatement(id, from, to);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "statement-" + id + ".pdf");
        headers.setContentLength(pdf.length);

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }


    @PostMapping("/{id}/email-statement")
    @Auditable(action = AuditAction.DOWNLOAD_STATEMENT, accountIdParam = "id", resourceType = "ACCOUNT")
    public ResponseEntity<Map<String, String>> emailStatement(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestBody(required = false) Map<String, String> body) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found: " + id));

        // Recipient: from body if provided, otherwise a placeholder
        String recipient = (body != null && body.get("email") != null && !body.get("email").isBlank())
                ? body.get("email")
                : account.getOwnerName().toLowerCase().replace(" ", "") + "@example.com";

        UUID requestId = UUID.randomUUID();

        eventPublisher.publishStatementRequested(new StatementRequestedEvent(
                account.getId(),
                account.getAccountNumber(),
                recipient,
                from,
                to,
                requestId
        ));

        return ResponseEntity.accepted().body(Map.of(
                "status", "accepted",
                "message", "Statement will be emailed to " + recipient,
                "requestId", requestId.toString()
        ));
    }
}