package com.smartbank.account.budget;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "budgets", indexes = {
        @Index(name = "idx_budget_user", columnList = "user_id"),
        @Index(name = "idx_budget_category", columnList = "category")
})
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "monthly_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyLimit;

    @Column(name = "alert_sent_80", nullable = false)
    private boolean alertSent80 = false;

    @Column(name = "alert_sent_100", nullable = false)
    private boolean alertSent100 = false;

    @Column(name = "last_reset_month", length = 7)
    private String lastResetMonth;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters & setters

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getMonthlyLimit() { return monthlyLimit; }
    public void setMonthlyLimit(BigDecimal monthlyLimit) { this.monthlyLimit = monthlyLimit; }

    public boolean isAlertSent80() { return alertSent80; }
    public void setAlertSent80(boolean alertSent80) { this.alertSent80 = alertSent80; }

    public boolean isAlertSent100() { return alertSent100; }
    public void setAlertSent100(boolean alertSent100) { this.alertSent100 = alertSent100; }

    public String getLastResetMonth() { return lastResetMonth; }
    public void setLastResetMonth(String lastResetMonth) { this.lastResetMonth = lastResetMonth; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}