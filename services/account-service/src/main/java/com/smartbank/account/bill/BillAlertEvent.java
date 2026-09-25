package com.smartbank.account.bill;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class BillAlertEvent {

    private UUID eventId;
    private LocalDateTime occurredAt;

    private UUID billId;
    private UUID accountId;
    private String accountNumber;
    private String billerName;
    private String category;
    private BigDecimal amount;
    private LocalDateTime dueDate;

    /** REMINDER, PAID, FAILED */
    private String alertType;

    public BillAlertEvent() {
        this.eventId = UUID.randomUUID();
        this.occurredAt = LocalDateTime.now();
    }

    public BillAlertEvent(UUID billId, UUID accountId, String accountNumber,
                          String billerName, String category, BigDecimal amount,
                          LocalDateTime dueDate, String alertType) {
        this();
        this.billId = billId;
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.billerName = billerName;
        this.category = category;
        this.amount = amount;
        this.dueDate = dueDate;
        this.alertType = alertType;
    }

    public UUID getEventId() { return eventId; }
    public LocalDateTime getOccurredAt() { return occurredAt; }

    public UUID getBillId() { return billId; }
    public UUID getAccountId() { return accountId; }
    public String getAccountNumber() { return accountNumber; }
    public String getBillerName() { return billerName; }
    public String getCategory() { return category; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getDueDate() { return dueDate; }
    public String getAlertType() { return alertType; }

    public void setBillId(UUID billId) { this.billId = billId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setBillerName(String billerName) { this.billerName = billerName; }
    public void setCategory(String category) { this.category = category; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
}