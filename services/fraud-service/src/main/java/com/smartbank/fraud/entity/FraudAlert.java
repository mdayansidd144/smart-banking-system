package com.smartbank.fraud.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fraud_alerts")
public class FraudAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID accountId;

    @Column
    private UUID relatedAccountId;   // for transfers: the destination

    @Column(nullable = false)
    private String eventType;         // DEPOSIT, WITHDRAWAL, TRANSFER

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String severity;          // LOW, MEDIUM, HIGH, CRITICAL

    @Column(nullable = false)
    private Integer riskScore;        // 0-100

    @Column(length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = AlertStatus.OPEN;
    }

    public enum AlertStatus {
        OPEN, REVIEWED, DISMISSED, CONFIRMED_FRAUD
    }

    // Getters & Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }

    public UUID getRelatedAccountId() { return relatedAccountId; }
    public void setRelatedAccountId(UUID relatedAccountId) { this.relatedAccountId = relatedAccountId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public AlertStatus getStatus() { return status; }
    public void setStatus(AlertStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}