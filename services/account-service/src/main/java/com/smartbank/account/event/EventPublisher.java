package com.smartbank.account.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

    // Topic names — keep them in one place
    public static final String TOPIC_ACCOUNT_CREATED = "account.created";
    public static final String TOPIC_MONEY_DEPOSITED = "money.deposited";
    public static final String TOPIC_MONEY_WITHDRAWN = "money.withdrawn";
    public static final String TOPIC_MONEY_TRANSFERRED = "money.transferred";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishAccountCreated(AccountCreatedEvent event) {
        kafkaTemplate.send(TOPIC_ACCOUNT_CREATED, event.getAccountId().toString(), event);
    }

    public void publishMoneyDeposited(MoneyDepositedEvent event) {
        kafkaTemplate.send(TOPIC_MONEY_DEPOSITED, event.getAccountId().toString(), event);
    }

    public void publishMoneyWithdrawn(MoneyWithdrawnEvent event) {
        kafkaTemplate.send(TOPIC_MONEY_WITHDRAWN, event.getAccountId().toString(), event);
    }

    public void publishMoneyTransferred(MoneyTransferredEvent event) {
        kafkaTemplate.send(TOPIC_MONEY_TRANSFERRED, event.getTransferId().toString(), event);
    }
}