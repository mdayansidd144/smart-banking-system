package com.smartbank.fraud.service;
import com.smartbank.fraud.entity.FraudAlert;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Component
public class FraudScoringEngine {
    // Thresholds (in INR)
    private static final BigDecimal LARGE_AMOUNT       = new BigDecimal("50000");
    private static final BigDecimal VERY_LARGE_AMOUNT  = new BigDecimal("200000");

    public static class ScoreResult {
        public int score;
        public List<String> reasons = new ArrayList<>();
        public String getSeverity() {
            if (score >= 80) return "CRITICAL";
            if (score >= 60) return "HIGH";
            if (score >= 40) return "MEDIUM";
            return "LOW";
        }
    }

    public ScoreResult score(String eventType,
                             BigDecimal amount,
                             UUID accountId,
                             UUID relatedAccountId,
                             LocalDateTime occurredAt) {
        ScoreResult result = new ScoreResult();
        // ---- Rule 1: Large amount ----
        if (amount.compareTo(VERY_LARGE_AMOUNT) >= 0) {
            result.score += 50;
            result.reasons.add("Very large amount: " + amount);
        } else if (amount.compareTo(LARGE_AMOUNT) >= 0) {
            result.score += 25;
            result.reasons.add("Large amount: " + amount);
        }
        // ---- Rule 2: Odd hours (11 PM - 5 AM) ----
        int hour = occurredAt.getHour();
        if (hour >= 23 || hour <= 5) {
            result.score += 20;
            result.reasons.add("Off-hours transaction at " + hour + ":00");
        }
        // ---- Rule 3: Round-amount withdrawals (common in fraud) ----
        if ("WITHDRAWAL".equals(eventType)
                && amount.remainder(new BigDecimal("10000")).compareTo(BigDecimal.ZERO) == 0
                && amount.compareTo(BigDecimal.ZERO) > 0) {
            result.score += 10;
            result.reasons.add("Round withdrawal amount (multiple of 10,000)");
        }
        // ---- Rule 4: Transfer to different account (slight risk bump) ----
        if ("TRANSFER".equals(eventType) && relatedAccountId != null) {
            result.score += 5;
            result.reasons.add("Inter-account transfer");
        }
        if (result.score > 100) result.score = 100;
        return result;
    }
}