package com.smartbank.account.bill;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class BillRequest {

    @NotNull(message = "Account ID is required")
    private UUID accountId;

    @NotNull(message = "Biller ID is required")
    private UUID billerId;

    private String nickname;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Amount must be positive")
    private BigDecimal amount;

    /** Cron expression — 6 fields (second minute hour day month day-of-week) */
    private String cronExpression = "0 0 10 1 * *";   // 1st of every month at 10 AM

    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }

    public UUID getBillerId() { return billerId; }
    public void setBillerId(UUID billerId) { this.billerId = billerId; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }
}