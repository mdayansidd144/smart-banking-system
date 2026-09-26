package com.smartbank.fraud.dto;
import java.math.BigDecimal;
public class AmortizationEntry {

    private int month;
    private BigDecimal emi;
    private BigDecimal principalComponent;
    private BigDecimal interestComponent;
    private BigDecimal remainingBalance;
    private BigDecimal cumulativeInterest;

    public AmortizationEntry(int month,
                             BigDecimal emi,
                             BigDecimal principalComponent,
                             BigDecimal interestComponent,
                             BigDecimal remainingBalance,
                             BigDecimal cumulativeInterest) {
        this.month = month;
        this.emi = emi;
        this.principalComponent = principalComponent;
        this.interestComponent = interestComponent;
        this.remainingBalance = remainingBalance;
        this.cumulativeInterest = cumulativeInterest;
    }

    public int getMonth() { return month; }
    public BigDecimal getEmi() { return emi; }
    public BigDecimal getPrincipalComponent() { return principalComponent; }
    public BigDecimal getInterestComponent() { return interestComponent; }
    public BigDecimal getRemainingBalance() { return remainingBalance; }
    public BigDecimal getCumulativeInterest() { return cumulativeInterest; }
}