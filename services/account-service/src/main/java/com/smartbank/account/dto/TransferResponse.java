package com.smartbank.account.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
public class TransferResponse {
    private UUID transferId;
    private UUID fromAccountId;
    private UUID toAccountId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;

    public TransferResponse(UUID transferId, UUID fromAccountId, UUID toAccountId,
                            BigDecimal amount, String status, LocalDateTime createdAt) {
        this.transferId = transferId;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }
    public UUID getTransferId() { return transferId; }
    public UUID getFromAccountId() { return fromAccountId; }
    public UUID getToAccountId() { return toAccountId; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}