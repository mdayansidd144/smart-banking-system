package com.smartbank.fraud.listener;
import com.smartbank.fraud.entity.AnomalyAlert;
import com.smartbank.fraud.event.AnomalyDetectedEvent;
import com.smartbank.fraud.event.MoneyDepositedEvent;
import com.smartbank.fraud.event.MoneyTransferredEvent;
import com.smartbank.fraud.event.MoneyWithdrawnEvent;
import com.smartbank.fraud.repository.AnomalyAlertRepository;
import com.smartbank.fraud.service.AnomalyRulesEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
@Component
public class AnomalyDetectionConsumer {
    private static final Logger log = LoggerFactory.getLogger(AnomalyDetectionConsumer.class);
    public static final String TOPIC_ANOMALY_DETECTED = "anomaly.detected";
    private final AnomalyRulesEngine engine;
    private final AnomalyAlertRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RestTemplate restTemplate = new RestTemplate();
    public AnomalyDetectionConsumer(AnomalyRulesEngine engine,
                                    AnomalyAlertRepository repository,
                                    KafkaTemplate<String, Object> kafkaTemplate) {
        this.engine = engine;
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(
            topics = "money.deposited",
            groupId = "anomaly-detection-group",
            properties = {
                    "spring.json.value.default.type=com.smartbank.fraud.event.MoneyDepositedEvent"
            }
    )
    public void onDeposit(@Payload MoneyDepositedEvent event) {
        check("DEPOSIT", event.getAccountId(), null,
                event.getAmount(), event.getOccurredAt());
    }

    @KafkaListener(
            topics = "money.withdrawn",
            groupId = "anomaly-detection-group",
            properties = {
                    "spring.json.value.default.type=com.smartbank.fraud.event.MoneyWithdrawnEvent"
            }
    )
    public void onWithdrawal(@Payload MoneyWithdrawnEvent event) {
        check("WITHDRAWAL", event.getAccountId(), null,
                event.getAmount(), event.getOccurredAt());
    }

    @KafkaListener(
            topics = "money.transferred",
            groupId = "anomaly-detection-group",
            properties = {
                    "spring.json.value.default.type=com.smartbank.fraud.event.MoneyTransferredEvent"
            }
    )
    public void onTransfer(@Payload MoneyTransferredEvent event) {
        check("TRANSFER", event.getFromAccountId(), event.getToAccountId(),
                event.getAmount(), event.getOccurredAt());
    }

    private void check(String eventType,
                       UUID accountId,
                       UUID relatedAccountId,
                       BigDecimal amount,
                       LocalDateTime occurredAt) {
        try {
            AnomalyRulesEngine.Result result =
                    engine.evaluate(eventType, accountId, relatedAccountId, amount, occurredAt);

            if (result == null) {
                log.debug("✔ No anomaly for {} on {} for ₹{}", eventType, accountId, amount);
                return;
            }

            AnomalyAlert alert = engine.toEntity(result, eventType, accountId,
                    relatedAccountId, amount);
            AnomalyAlert saved = repository.save(alert);

            AnomalyDetectedEvent detected = new AnomalyDetectedEvent(
                    saved.getId(),
                    saved.getAccountId(),
                    saved.getRelatedAccountId(),
                    saved.getEventType(),
                    saved.getRuleTriggered(),
                    saved.getSeverity().name(),
                    saved.getRiskScore(),
                    saved.getAmount(),
                    saved.getReason()
            );
            kafkaTemplate.send(TOPIC_ANOMALY_DETECTED,
                    saved.getId().toString(), detected);

            log.warn(" ANOMALY STORED [{}] {} on {} — rule={}",
                    saved.getSeverity(),
                    saved.getEventType(),
                    saved.getAccountId(),
                    saved.getRuleTriggered());

            // AUTO-FREEZE if CRITICAL severity
            if ("CRITICAL".equals(saved.getSeverity().name())) {
                autoFreezeAccount(saved.getAccountId(),
                        "Critical anomaly: " + saved.getRuleTriggered());
            }

        } catch (Exception e) {
            log.error(" Anomaly check failed for {} on {}: {}",
                    eventType, accountId, e.getMessage(), e);
        }
    }
    private void autoFreezeAccount(UUID accountId, String reason) {
        try {
            String url = "http://account-service:8080/api/v1/accounts/"
                    + accountId + "/freeze";
            Map<String, String> body = Map.of("reason", "AUTO-FREEZE: " + reason);
            restTemplate.postForEntity(url, body, String.class);
            log.warn("️ AUTO-FROZE account {} — reason: {}", accountId, reason);
        } catch (Exception e) {
            log.error("Failed to auto-freeze account {}: {}", accountId, e.getMessage());
        }
    }
}