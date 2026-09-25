package com.smartbank.account.budget;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
public class BudgetAlertEvent {

    private UUID eventId;
    private LocalDateTime occurredAt;
    private UUID budgetId;
    private String userId;
    private String category;
    private BigDecimal monthlyLimit;
    private BigDecimal currentSpend;
    private double percentageUsed;
    private String status;   // WARNING, EXCEEDED

    public BudgetAlertEvent() {
        this.eventId = UUID.randomUUID();
        this.occurredAt = LocalDateTime.now();
    }

    public BudgetAlertEvent(UUID budgetId, String userId, String category,
                            BigDecimal monthlyLimit, BigDecimal currentSpend,
                            double percentageUsed, String status) {
        this();
        this.budgetId = budgetId;
        this.userId = userId;
        this.category = category;
        this.monthlyLimit = monthlyLimit;
        this.currentSpend = currentSpend;
        this.percentageUsed = percentageUsed;
        this.status = status;
    }

    public UUID getEventId() { return eventId; }
    public LocalDateTime getOccurredAt() { return occurredAt; }

    public UUID getBudgetId() { return budgetId; }
    public void setBudgetId(UUID budgetId) { this.budgetId = budgetId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getMonthlyLimit() { return monthlyLimit; }
    public void setMonthlyLimit(BigDecimal monthlyLimit) { this.monthlyLimit = monthlyLimit; }

    public BigDecimal getCurrentSpend() { return currentSpend; }
    public void setCurrentSpend(BigDecimal currentSpend) { this.currentSpend = currentSpend; }

    public double getPercentageUsed() { return percentageUsed; }
    public void setPercentageUsed(double percentageUsed) { this.percentageUsed = percentageUsed; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}