package com.smartbank.fraud.event;
import java.math.BigDecimal;
import java.util.UUID;
public class AnomalyDetectedEvent extends BaseEvent {

    private UUID anomalyId;
    private UUID accountId;
    private UUID relatedAccountId;
    private String eventType;
    private String ruleTriggered;
    private String severity;
    private Integer riskScore;
    private BigDecimal amount;
    private String reason;

    public AnomalyDetectedEvent() {}

    public AnomalyDetectedEvent(UUID anomalyId, UUID accountId, UUID relatedAccountId,
                                String eventType, String ruleTriggered, String severity,
                                Integer riskScore, BigDecimal amount, String reason) {
        this.anomalyId = anomalyId;
        this.accountId = accountId;
        this.relatedAccountId = relatedAccountId;
        this.eventType = eventType;
        this.ruleTriggered = ruleTriggered;
        this.severity = severity;
        this.riskScore = riskScore;
        this.amount = amount;
        this.reason = reason;
    }

    public UUID getAnomalyId() { return anomalyId; }
    public UUID getAccountId() { return accountId; }
    public UUID getRelatedAccountId() { return relatedAccountId; }
    public String getEventType() { return eventType; }
    public String getRuleTriggered() { return ruleTriggered; }
    public String getSeverity() { return severity; }
    public Integer getRiskScore() { return riskScore; }
    public BigDecimal getAmount() { return amount; }
    public String getReason() { return reason; }
}