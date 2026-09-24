package com.smartbank.fraud.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class LoanApplicationResponse {

    private UUID id;
    private UUID accountId;
    private BigDecimal requestedAmount;
    private Integer termMonths;
    private String purpose;
    private String decision;
    private BigDecimal approvedAmount;
    private BigDecimal interestRate;
    private Integer riskScore;
    private String reasoning;
    private LocalDateTime createdAt;

    public LoanApplicationResponse(UUID id, UUID accountId, BigDecimal requestedAmount,
                                   Integer termMonths, String purpose, String decision,
                                   BigDecimal approvedAmount, BigDecimal interestRate,
                                   Integer riskScore, String reasoning, LocalDateTime createdAt) {
        this.id = id;
        this.accountId = accountId;
        this.requestedAmount = requestedAmount;
        this.termMonths = termMonths;
        this.purpose = purpose;
        this.decision = decision;
        this.approvedAmount = approvedAmount;
        this.interestRate = interestRate;
        this.riskScore = riskScore;
        this.reasoning = reasoning;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getAccountId() { return accountId; }
    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public Integer getTermMonths() { return termMonths; }
    public String getPurpose() { return purpose; }
    public String getDecision() { return decision; }
    public BigDecimal getApprovedAmount() { return approvedAmount; }
    public BigDecimal getInterestRate() { return interestRate; }
    public Integer getRiskScore() { return riskScore; }
    public String getReasoning() { return reasoning; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}