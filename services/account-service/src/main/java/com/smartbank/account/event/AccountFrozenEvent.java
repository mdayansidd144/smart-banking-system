package com.smartbank.account.event;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class AccountFrozenEvent {

    private UUID eventId;
    private LocalDateTime occurredAt;
    private UUID accountId;
    private String accountNumber;
    private String ownerName;
    private BigDecimal balance;
    private String reason;

    public AccountFrozenEvent() {
        this.eventId = UUID.randomUUID();
        this.occurredAt = LocalDateTime.now();
    }

    public AccountFrozenEvent(UUID accountId, String accountNumber,
                              String ownerName, BigDecimal balance, String reason) {
        this();
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = balance;
        this.reason = reason;
    }

    public UUID getEventId() { return eventId; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public UUID getAccountId() { return accountId; }
    public String getAccountNumber() { return accountNumber; }
    public String getOwnerName() { return ownerName; }
    public BigDecimal getBalance() { return balance; }
    public String getReason() { return reason; }

    public void setAccountId(UUID accountId) { this.accountId = accountId; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public void setReason(String reason) { this.reason = reason; }
}