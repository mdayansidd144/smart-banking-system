package com.smartbank.fraud.service;
import com.smartbank.fraud.dto.AmortizationEntry;
import com.smartbank.fraud.dto.AmortizationSchedule;
import com.smartbank.fraud.entity.LoanApplication;
import com.smartbank.fraud.repository.LoanApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AmortizationService {

    private static final MathContext MC = new MathContext(12, RoundingMode.HALF_UP);
    private static final int SCALE = 2;

    private final LoanApplicationRepository repository;

    public AmortizationService(LoanApplicationRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public AmortizationSchedule generate(UUID loanId) {
        LoanApplication loan = repository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found: " + loanId));

        if (loan.getDecision() != LoanApplication.LoanDecision.APPROVED) {
            throw new RuntimeException(
                    "Amortization schedule is only available for APPROVED loans. Current: "
                            + loan.getDecision());
        }

        BigDecimal principal = loan.getApprovedAmount();
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Approved amount is invalid for loan " + loanId);
        }

        BigDecimal annualRate = loan.getInterestRate();
        if (annualRate == null) {
            throw new RuntimeException("Interest rate missing for loan " + loanId);
        }

        int n = loan.getTermMonths();

        // Monthly rate = annual% / 12 / 100
        BigDecimal monthlyRate = annualRate
                .divide(BigDecimal.valueOf(12), MC)
                .divide(BigDecimal.valueOf(100), MC);

        // EMI = P * r * (1+r)^n / ((1+r)^n - 1)
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate, MC);
        BigDecimal onePlusRPowN = onePlusR.pow(n, MC);
        BigDecimal emi = principal
                .multiply(monthlyRate, MC)
                .multiply(onePlusRPowN, MC)
                .divide(onePlusRPowN.subtract(BigDecimal.ONE, MC), MC)
                .setScale(SCALE, RoundingMode.HALF_UP);

        // Build month-by-month schedule
        List<AmortizationEntry> entries = new ArrayList<>(n);
        BigDecimal balance = principal;
        BigDecimal cumulativeInterest = BigDecimal.ZERO;

        for (int month = 1; month <= n; month++) {
            BigDecimal interest = balance.multiply(monthlyRate, MC)
                    .setScale(SCALE, RoundingMode.HALF_UP);
            BigDecimal principalComponent = emi.subtract(interest)
                    .setScale(SCALE, RoundingMode.HALF_UP);
            balance = balance.subtract(principalComponent)
                    .setScale(SCALE, RoundingMode.HALF_UP);
            cumulativeInterest = cumulativeInterest.add(interest)
                    .setScale(SCALE, RoundingMode.HALF_UP);

            if (month == n && balance.compareTo(BigDecimal.ZERO) != 0) {
                // Adjust final principal so balance lands on 0
                principalComponent = principalComponent.add(balance)
                        .setScale(SCALE, RoundingMode.HALF_UP);
                balance = BigDecimal.ZERO;
            }

            entries.add(new AmortizationEntry(
                    month,
                    emi,
                    principalComponent,
                    interest,
                    balance,
                    cumulativeInterest
            ));
        }

        BigDecimal totalPayment = emi.multiply(BigDecimal.valueOf(n))
                .setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal totalInterest = totalPayment.subtract(principal)
                .setScale(SCALE, RoundingMode.HALF_UP);

        return new AmortizationSchedule(
                loan.getId(),
                loan.getAccountId(),
                principal,
                annualRate,
                n,
                emi,
                totalInterest,
                totalPayment,
                entries
        );
    }
}