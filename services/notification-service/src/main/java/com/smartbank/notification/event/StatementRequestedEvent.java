package com.smartbank.notification.event;
import java.time.LocalDate;
import java.util.UUID;
public class StatementRequestedEvent {
    private UUID eventId;
    private java.time.LocalDateTime occurredAt;
    private UUID accountId;
    private String accountNumber;
    private String recipientEmail;
    private LocalDate fromDate;
    private LocalDate toDate;
    private UUID requestId;

    public StatementRequestedEvent() {
        this.eventId = UUID.randomUUID();
        this.occurredAt = java.time.LocalDateTime.now();
    }

    public UUID getEventId() { return eventId; }
    public java.time.LocalDateTime getOccurredAt() { return occurredAt; }

    public UUID getAccountId() { return accountId; }
    public String getAccountNumber() { return accountNumber; }
    public String getRecipientEmail() { return recipientEmail; }
    public LocalDate getFromDate() { return fromDate; }
    public LocalDate getToDate() { return toDate; }
    public UUID getRequestId() { return requestId; }
}