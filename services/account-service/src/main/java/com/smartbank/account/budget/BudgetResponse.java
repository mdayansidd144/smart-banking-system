package com.smartbank.account.budget;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
public class BudgetResponse {

    private UUID id;
    private String category;
    private BigDecimal monthlyLimit;
    private BigDecimal currentSpend;
    private double percentageUsed;
    private String status;   // OK, WARNING, EXCEEDED
    private LocalDateTime createdAt;

    public BudgetResponse(UUID id, String category, BigDecimal monthlyLimit,
                          BigDecimal currentSpend, double percentageUsed,
                          String status, LocalDateTime createdAt) {
        this.id = id;
        this.category = category;
        this.monthlyLimit = monthlyLimit;
        this.currentSpend = currentSpend;
        this.percentageUsed = percentageUsed;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getCategory() { return category; }
    public BigDecimal getMonthlyLimit() { return monthlyLimit; }
    public BigDecimal getCurrentSpend() { return currentSpend; }
    public double getPercentageUsed() { return percentageUsed; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}