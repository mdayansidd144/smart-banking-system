package com.smartbank.notification.event;

import java.math.BigDecimal;
import java.util.UUID;

public class MoneyTransferredEvent extends BaseEvent {

    private UUID transferId;
    private UUID fromAccountId;
    private UUID toAccountId;
    private BigDecimal amount;
    private String description;

    public MoneyTransferredEvent() {}

    public MoneyTransferredEvent(UUID transferId, UUID fromAccountId, UUID toAccountId,
                                 BigDecimal amount, String description) {
        this.transferId = transferId;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.description = description;
    }

    public UUID getTransferId() { return transferId; }
    public UUID getFromAccountId() { return fromAccountId; }
    public UUID getToAccountId() { return toAccountId; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
}