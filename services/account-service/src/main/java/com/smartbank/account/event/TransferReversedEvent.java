package com.smartbank.account.event;
import java.math.BigDecimal;
import java.util.UUID;
public class TransferReversedEvent extends BaseEvent {

    private UUID transferId;
    private UUID fromAccountId;
    private UUID toAccountId;
    private BigDecimal amount;
    private String reason;

    public TransferReversedEvent() {}

    public TransferReversedEvent(UUID transferId, UUID fromAccountId, UUID toAccountId,
                                 BigDecimal amount, String reason) {
        this.transferId = transferId;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.reason = reason;
    }

    public UUID getTransferId() { return transferId; }
    public UUID getFromAccountId() { return fromAccountId; }
    public UUID getToAccountId() { return toAccountId; }
    public BigDecimal getAmount() { return amount; }
    public String getReason() { return reason; }
}