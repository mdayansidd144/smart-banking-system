package com.smartbank.account.bill;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bills")
public class BillController {

    private final BillService service;
    private final BillerRepository billerRepository;

    public BillController(BillService service, BillerRepository billerRepository) {
        this.service = service;
        this.billerRepository = billerRepository;
    }

    @GetMapping("/billers")
    public ResponseEntity<List<Biller>> listBillers() {
        return ResponseEntity.ok(billerRepository.findAllByOrderByNameAsc());
    }

    @PostMapping
    public ResponseEntity<BillResponse> create(@Valid @RequestBody BillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    public ResponseEntity<List<BillResponse>> listAll() {
        return ResponseEntity.ok(service.listAll());
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<BillResponse>> listByAccount(@PathVariable UUID accountId) {
        return ResponseEntity.ok(service.listByAccount(accountId));
    }

    @PatchMapping("/{id}/pause")
    public ResponseEntity<BillResponse> pause(@PathVariable UUID id) {
        return ResponseEntity.ok(service.pause(id));
    }

    @PatchMapping("/{id}/resume")
    public ResponseEntity<BillResponse> resume(@PathVariable UUID id) {
        return ResponseEntity.ok(service.resume(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> cancel(@PathVariable UUID id) {
        service.cancel(id);
        return ResponseEntity.ok(Map.of("message", "Bill cancelled"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleError(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}