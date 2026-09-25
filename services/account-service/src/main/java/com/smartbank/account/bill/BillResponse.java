package com.smartbank.account.bill;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class BillResponse {

    private UUID id;
    private UUID accountId;
    private UUID billerId;
    private String billerName;
    private String billerCategory;
    private String nickname;
    private BigDecimal amount;
    private String cronExpression;
    private LocalDateTime nextDueAt;
    private LocalDateTime lastPaidAt;
    private UUID lastTransferId;
    private String status;
    private Integer successCount;
    private Integer failureCount;
    private String lastError;
    private LocalDateTime createdAt;

    public BillResponse(UUID id, UUID accountId, UUID billerId, String billerName,
                        String billerCategory, String nickname, BigDecimal amount,
                        String cronExpression, LocalDateTime nextDueAt,
                        LocalDateTime lastPaidAt, UUID lastTransferId, String status,
                        Integer successCount, Integer failureCount, String lastError,
                        LocalDateTime createdAt) {
        this.id = id;
        this.accountId = accountId;
        this.billerId = billerId;
        this.billerName = billerName;
        this.billerCategory = billerCategory;
        this.nickname = nickname;
        this.amount = amount;
        this.cronExpression = cronExpression;
        this.nextDueAt = nextDueAt;
        this.lastPaidAt = lastPaidAt;
        this.lastTransferId = lastTransferId;
        this.status = status;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.lastError = lastError;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getAccountId() { return accountId; }
    public UUID getBillerId() { return billerId; }
    public String getBillerName() { return billerName; }
    public String getBillerCategory() { return billerCategory; }
    public String getNickname() { return nickname; }
    public BigDecimal getAmount() { return amount; }
    public String getCronExpression() { return cronExpression; }
    public LocalDateTime getNextDueAt() { return nextDueAt; }
    public LocalDateTime getLastPaidAt() { return lastPaidAt; }
    public UUID getLastTransferId() { return lastTransferId; }
    public String getStatus() { return status; }
    public Integer getSuccessCount() { return successCount; }
    public Integer getFailureCount() { return failureCount; }
    public String getLastError() { return lastError; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}