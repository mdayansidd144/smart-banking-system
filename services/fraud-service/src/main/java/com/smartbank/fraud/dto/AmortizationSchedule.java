package com.smartbank.fraud.dto;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class AmortizationSchedule {

    private UUID loanId;
    private UUID accountId;
    private BigDecimal principal;
    private BigDecimal annualInterestRate;
    private int termMonths;
    private BigDecimal monthlyEmi;
    private BigDecimal totalInterest;
    private BigDecimal totalPayment;
    private List<AmortizationEntry> entries;

    public AmortizationSchedule(UUID loanId,
                                UUID accountId,
                                BigDecimal principal,
                                BigDecimal annualInterestRate,
                                int termMonths,
                                BigDecimal monthlyEmi,
                                BigDecimal totalInterest,
                                BigDecimal totalPayment,
                                List<AmortizationEntry> entries) {
        this.loanId = loanId;
        this.accountId = accountId;
        this.principal = principal;
        this.annualInterestRate = annualInterestRate;
        this.termMonths = termMonths;
        this.monthlyEmi = monthlyEmi;
        this.totalInterest = totalInterest;
        this.totalPayment = totalPayment;
        this.entries = entries;
    }

    public UUID getLoanId() { return loanId; }
    public UUID getAccountId() { return accountId; }
    public BigDecimal getPrincipal() { return principal; }
    public BigDecimal getAnnualInterestRate() { return annualInterestRate; }
    public int getTermMonths() { return termMonths; }
    public BigDecimal getMonthlyEmi() { return monthlyEmi; }
    public BigDecimal getTotalInterest() { return totalInterest; }
    public BigDecimal getTotalPayment() { return totalPayment; }
    public List<AmortizationEntry> getEntries() { return entries; }
}