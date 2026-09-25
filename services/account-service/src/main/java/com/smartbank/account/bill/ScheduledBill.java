package com.smartbank.account.bill;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "scheduled_bills", indexes = {
        @Index(name = "idx_bill_account", columnList = "account_id"),
        @Index(name = "idx_bill_status", columnList = "status"),
        @Index(name = "idx_bill_next_due", columnList = "next_due_at")
})
public class ScheduledBill {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "biller_id", nullable = false)
    private UUID billerId;

    @Column(length = 100)
    private String nickname;   // e.g., "Home Electricity"

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "cron_expression", nullable = false, length = 100)
    private String cronExpression;

    @Column(name = "next_due_at", nullable = false)
    private LocalDateTime nextDueAt;

    @Column(name = "last_paid_at")
    private LocalDateTime lastPaidAt;

    @Column(name = "last_transfer_id")
    private UUID lastTransferId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillStatus status;

    @Column(name = "success_count", nullable = false)
    private Integer successCount = 0;

    @Column(name = "failure_count", nullable = false)
    private Integer failureCount = 0;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(name = "reminder_sent_for", length = 30)
    private String reminderSentFor;   // tracks which month reminder was sent (YYYY-MM)

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = BillStatus.ACTIVE;
        if (this.successCount == null) this.successCount = 0;
        if (this.failureCount == null) this.failureCount = 0;
    }

    // Getters & setters

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

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

    public LocalDateTime getNextDueAt() { return nextDueAt; }
    public void setNextDueAt(LocalDateTime nextDueAt) { this.nextDueAt = nextDueAt; }

    public LocalDateTime getLastPaidAt() { return lastPaidAt; }
    public void setLastPaidAt(LocalDateTime lastPaidAt) { this.lastPaidAt = lastPaidAt; }

    public UUID getLastTransferId() { return lastTransferId; }
    public void setLastTransferId(UUID lastTransferId) { this.lastTransferId = lastTransferId; }

    public BillStatus getStatus() { return status; }
    public void setStatus(BillStatus status) { this.status = status; }

    public Integer getSuccessCount() { return successCount; }
    public void setSuccessCount(Integer successCount) { this.successCount = successCount; }

    public Integer getFailureCount() { return failureCount; }
    public void setFailureCount(Integer failureCount) { this.failureCount = failureCount; }

    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }

    public String getReminderSentFor() { return reminderSentFor; }
    public void setReminderSentFor(String reminderSentFor) { this.reminderSentFor = reminderSentFor; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}