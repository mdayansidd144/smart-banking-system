package com.smartbank.account.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
public class AccountResponse {
    private UUID id;
    private String ownerName;
    private String accountNumber;
    private BigDecimal balance;
    private String currency;
    private String status;
    private LocalDateTime createdAt;

    public AccountResponse(UUID id, String ownerName, String accountNumber,
                           BigDecimal balance, String currency,
                           String status, LocalDateTime createdAt) {
        this.id = id;
        this.ownerName = ownerName;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.currency = currency;
        this.status = status;
        this.createdAt = createdAt;
    }
    public UUID getId() { return id; }
    public String getOwnerName() { return ownerName; }
    public String getAccountNumber() { return accountNumber; }
    public BigDecimal getBalance() { return balance; }
    public String getCurrency() { return currency; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}