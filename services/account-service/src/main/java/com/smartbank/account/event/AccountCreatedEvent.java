package com.smartbank.account.event;
import java.util.UUID;
public class AccountCreatedEvent extends BaseEvent {
    private UUID accountId;
    private String ownerName;
    private String accountNumber;
    private String currency;

    public AccountCreatedEvent() {}

    public AccountCreatedEvent(UUID accountId, String ownerName,
                               String accountNumber, String currency) {
        this.accountId = accountId;
        this.ownerName = ownerName;
        this.accountNumber = accountNumber;
        this.currency = currency;
    }
    public UUID getAccountId() { return accountId; }
    public String getOwnerName() { return ownerName; }
    public String getAccountNumber() { return accountNumber; }
    public String getCurrency() { return currency; }
}