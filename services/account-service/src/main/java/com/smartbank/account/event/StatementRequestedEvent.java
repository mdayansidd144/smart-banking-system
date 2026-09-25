package com.smartbank.account.event;
import java.time.LocalDate;
import java.util.UUID;
public class StatementRequestedEvent extends BaseEvent {
    private UUID accountId;
    private String accountNumber;
    private String recipientEmail;
    private LocalDate fromDate;
    private LocalDate toDate;
    private UUID requestId;

    public StatementRequestedEvent() {}

    public StatementRequestedEvent(UUID accountId, String accountNumber,
                                   String recipientEmail,
                                   LocalDate fromDate, LocalDate toDate,
                                   UUID requestId) {
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.recipientEmail = recipientEmail;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.requestId = requestId;
    }

    public UUID getAccountId() { return accountId; }
    public String getAccountNumber() { return accountNumber; }
    public String getRecipientEmail() { return recipientEmail; }
    public LocalDate getFromDate() { return fromDate; }
    public LocalDate getToDate() { return toDate; }
    public UUID getRequestId() { return requestId; }
}