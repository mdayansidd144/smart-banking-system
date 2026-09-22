package com.smartbank.fraud.listener;
import com.smartbank.fraud.entity.FraudAlert;
import com.smartbank.fraud.event.MoneyDepositedEvent;
import com.smartbank.fraud.event.MoneyTransferredEvent;
import com.smartbank.fraud.event.MoneyWithdrawnEvent;
import com.smartbank.fraud.repository.FraudAlertRepository;
import com.smartbank.fraud.service.FraudScoringEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
@Component
public class FraudEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(FraudEventConsumer.class);

    private final FraudScoringEngine engine;
    private final FraudAlertRepository alertRepository;
    public FraudEventConsumer(FraudScoringEngine engine,
                              FraudAlertRepository alertRepository) {
        this.engine = engine;
        this.alertRepository = alertRepository;
    }

    @KafkaListener(
            topics = "money.deposited",
            groupId = "fraud-service-group",
            properties = {
                    "spring.json.value.default.type=com.smartbank.fraud.event.MoneyDepositedEvent"
            }
    )
    public void onDeposit(@Payload MoneyDepositedEvent event) {
        process("DEPOSIT", event.getAccountId(), null,
                event.getAmount(), event.getOccurredAt());
    }

    @KafkaListener(
            topics = "money.withdrawn",
            groupId = "fraud-service-group",
            properties = {
                    "spring.json.value.default.type=com.smartbank.fraud.event.MoneyWithdrawnEvent"
            }
    )
    public void onWithdrawal(@Payload MoneyWithdrawnEvent event) {
        process("WITHDRAWAL", event.getAccountId(), null,
                event.getAmount(), event.getOccurredAt());
    }
    @KafkaListener(
            topics = "money.transferred",
            groupId = "fraud-service-group",
            properties = {
                    "spring.json.value.default.type=com.smartbank.fraud.event.MoneyTransferredEvent"
            }
    )
    public void onTransfer(@Payload MoneyTransferredEvent event) {
        process("TRANSFER", event.getFromAccountId(), event.getToAccountId(),
                event.getAmount(), event.getOccurredAt());
    }
    private void process(String eventType, UUID accountId, UUID relatedAccountId,
                         BigDecimal amount, LocalDateTime occurredAt) {

        FraudScoringEngine.ScoreResult result =
                engine.score(eventType, amount, accountId, relatedAccountId, occurredAt);

        // Only persist MEDIUM and above — ignore noise
        if (result.score < 40) {
            log.debug("✔ Low-risk {} on {} for ₹{}. Score={}",
                    eventType, accountId, amount, result.score);
            return;
        }

        FraudAlert alert = new FraudAlert();
        alert.setAccountId(accountId);
        alert.setRelatedAccountId(relatedAccountId);
        alert.setEventType(eventType);
        alert.setAmount(amount);
        alert.setRiskScore(result.score);
        alert.setSeverity(result.getSeverity());
        alert.setReason(String.join("; ", result.reasons));

        alertRepository.save(alert);

        log.warn("️FRAUD ALERT [{}] {} on account {} for ₹{}. Score={} | {}",
                result.getSeverity(),
                eventType,
                accountId,
                amount,
                result.score,
                String.join(" | ", result.reasons));
    }
}