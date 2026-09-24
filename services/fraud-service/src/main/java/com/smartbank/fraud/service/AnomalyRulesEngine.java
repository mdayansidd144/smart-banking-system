package com.smartbank.fraud.service;
import com.smartbank.fraud.entity.AnomalyAlert;
import com.smartbank.fraud.entity.AnomalySeverity;
import com.smartbank.fraud.repository.AnomalyAlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AnomalyRulesEngine {

    private static final Logger log = LoggerFactory.getLogger(AnomalyRulesEngine.class);

    // ---- Rule thresholds (tune as needed) ----
    private static final int    VELOCITY_TX_COUNT     = 5;      // > 5 tx / 60s
    private static final long   VELOCITY_WINDOW_MIN   = 1;
    private static final int    REVERSAL_COUNT        = 2;      // > 2 reversals / 60min
    private static final long   REVERSAL_WINDOW_MIN   = 60;
    private static final BigDecimal NEW_ACCOUNT_LARGE_TX = new BigDecimal("50000");
    private static final int    NEW_ACCOUNT_AGE_DAYS  = 7;
    private static final BigDecimal ROUND_NUMBER_THRESHOLD = new BigDecimal("100000");
    private static final BigDecimal ROUND_NUMBER_STEP  = new BigDecimal("10000");
    private static final BigDecimal STRUCTURING_THRESHOLD = new BigDecimal("50000");
    private static final int    STRUCTURING_COUNT     = 3;      // 3+ tx just under threshold

    private final AnomalyAlertRepository repository;

    public AnomalyRulesEngine(AnomalyAlertRepository repository) {
        this.repository = repository;
    }

    public Result evaluate(String eventType,
                           UUID accountId,
                           UUID relatedAccountId,
                           BigDecimal amount,
                           LocalDateTime occurredAt) {

        List<Match> matches = new ArrayList<>();

        // Rule 1 — Rapid velocity
        if (eventType.equals("TRANSFER") || eventType.equals("WITHDRAWAL")) {
            LocalDateTime since = occurredAt.minusMinutes(VELOCITY_WINDOW_MIN);
            long recent = repository.countRecentByAccount(accountId, since);
            if (recent >= VELOCITY_TX_COUNT) {
                matches.add(new Match("RAPID_VELOCITY", AnomalySeverity.HIGH, 75,
                        "More than " + VELOCITY_TX_COUNT + " transactions in "
                                + VELOCITY_WINDOW_MIN + " minute(s) on account " + accountId));
            }
        }

        // Rule 2 — New account + large transaction
        if (amount.compareTo(NEW_ACCOUNT_LARGE_TX) > 0) {
            LocalDateTime since = occurredAt.minusDays(NEW_ACCOUNT_AGE_DAYS);
            long prior = repository.countRecentByAccount(accountId, since);
            if (prior <= 1) {
                matches.add(new Match("NEW_ACCOUNT_LARGE_TX", AnomalySeverity.HIGH, 70,
                        "Large transaction ₹" + amount + " with little prior account activity"));
            }
        }

        // Rule 3 — Round-number large transaction (common in structuring / layering)
        if (amount.compareTo(ROUND_NUMBER_THRESHOLD) >= 0
                && amount.remainder(ROUND_NUMBER_STEP).compareTo(BigDecimal.ZERO) == 0) {
            matches.add(new Match("ROUND_NUMBER_LARGE", AnomalySeverity.MEDIUM, 50,
                    "Large round-number transaction of ₹" + amount));
        }

        // Rule 4 — Odd-hours activity (2 AM – 5 AM)
        LocalTime localTime = occurredAt.toLocalTime();
        if (localTime.isAfter(LocalTime.of(2, 0))
                && localTime.isBefore(LocalTime.of(5, 0))) {
            matches.add(new Match("ODD_HOURS_ACTIVITY", AnomalySeverity.LOW, 35,
                    "Transaction at unusual hour: " + localTime));
        }

        // Rule 5 — Rapid reversals (only for REVERSAL event)
        if (eventType.equals("REVERSAL")) {
            LocalDateTime since = occurredAt.minusMinutes(REVERSAL_WINDOW_MIN);
            long recent = repository.countRecentByAccount(accountId, since);
            if (recent >= REVERSAL_COUNT) {
                matches.add(new Match("RAPID_REVERSALS", AnomalySeverity.CRITICAL, 90,
                        "More than " + REVERSAL_COUNT + " reversals within "
                                + REVERSAL_WINDOW_MIN + " minutes"));
            }
        }

        // Rule 6 — Structuring: multiple txs just under the reporting threshold
        if (amount.compareTo(STRUCTURING_THRESHOLD) < 0
                && amount.compareTo(STRUCTURING_THRESHOLD.multiply(new BigDecimal("0.9"))) >= 0) {
            LocalDateTime since = occurredAt.minusHours(24);
            long recent = repository.countRecentByAccount(accountId, since);
            if (recent >= STRUCTURING_COUNT) {
                matches.add(new Match("STRUCTURING", AnomalySeverity.CRITICAL, 92,
                        "Multiple transactions just under ₹" + STRUCTURING_THRESHOLD
                                + " threshold within 24h — possible structuring"));
            }
        }
        if (matches.isEmpty()) {
            return null;
        }
        // Return highest severity
        Match top = matches.stream()
                .max((a, b) -> Integer.compare(a.severity.ordinal(), b.severity.ordinal()))
                .orElse(matches.get(0));

        log.info(" ANOMALY [{}] {} on account {} for ₹{} — {}",
                top.severity, eventType, accountId, amount, top.reason);
        return new Result(top.ruleName, top.severity, top.riskScore, top.reason);
    }
    private record Match(String ruleName, AnomalySeverity severity,
                         int riskScore, String reason) {}

    public record Result(String ruleTriggered, AnomalySeverity severity,
                         int riskScore, String reason) {}

    /** Convenience: build an AnomalyAlert entity from a Result */
    public AnomalyAlert toEntity(Result result, String eventType, UUID accountId,
                                 UUID relatedAccountId, BigDecimal amount) {
        AnomalyAlert a = new AnomalyAlert();
        a.setAccountId(accountId);
        a.setRelatedAccountId(relatedAccountId);
        a.setEventType(eventType);
        a.setRuleTriggered(result.ruleTriggered());
        a.setAmount(amount);
        a.setSeverity(result.severity());
        a.setRiskScore(result.riskScore());
        a.setReason(result.reason());
        return a;
    }
}