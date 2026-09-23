package com.smartbank.account.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ScheduledTransferResponse {

    private UUID id;
    private UUID fromAccountId;
    private UUID toAccountId;
    private BigDecimal amount;
    private String description;
    private String cronExpression;
    private String status;
    private LocalDateTime nextRunAt;
    private LocalDateTime lastRunAt;
    private UUID lastTransferId;
    private Integer successCount;
    private Integer failureCount;
    private String lastError;
    private LocalDateTime createdAt;

    public ScheduledTransferResponse(UUID id, UUID fromAccountId, UUID toAccountId,
                                     BigDecimal amount, String description, String cronExpression,
                                     String status, LocalDateTime nextRunAt, LocalDateTime lastRunAt,
                                     UUID lastTransferId, Integer successCount, Integer failureCount,
                                     String lastError, LocalDateTime createdAt) {
        this.id = id;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.description = description;
        this.cronExpression = cronExpression;
        this.status = status;
        this.nextRunAt = nextRunAt;
        this.lastRunAt = lastRunAt;
        this.lastTransferId = lastTransferId;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.lastError = lastError;
        this.createdAt = createdAt;
    }

    // Getters

    public UUID getId() { return id; }
    public UUID getFromAccountId() { return fromAccountId; }
    public UUID getToAccountId() { return toAccountId; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
    public String getCronExpression() { return cronExpression; }
    public String getStatus() { return status; }
    public LocalDateTime getNextRunAt() { return nextRunAt; }
    public LocalDateTime getLastRunAt() { return lastRunAt; }
    public UUID getLastTransferId() { return lastTransferId; }
    public Integer getSuccessCount() { return successCount; }
    public Integer getFailureCount() { return failureCount; }
    public String getLastError() { return lastError; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}