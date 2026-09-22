package com.smartbank.notification;
import com.smartbank.notification.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
@Component
public class EventConsumer {
    private static final Logger log = LoggerFactory.getLogger(EventConsumer.class);
    @KafkaListener(
            topics = "account.created",
            groupId = "notification-group",
            properties = {
                    "spring.json.value.default.type=com.smartbank.notification.event.AccountCreatedEvent"
            }
    )
    public void onAccountCreated(@Payload AccountCreatedEvent event) {
        log.info(" [EVENT] New account created: owner={}, accountNumber={}",
                event.getOwnerName(), event.getAccountNumber());
    }

    @KafkaListener(
            topics = "money.deposited",
            groupId = "notification-group",
            properties = {
                    "spring.json.value.default.type=com.smartbank.notification.event.MoneyDepositedEvent"
            }
    )
    public void onMoneyDeposited(@Payload MoneyDepositedEvent event) {
        log.info(" [EVENT] Deposit: account={}, amount={}, balanceAfter={}",
                event.getAccountId(), event.getAmount(), event.getBalanceAfter());
    }

    @KafkaListener(
            topics = "money.withdrawn",
            groupId = "notification-group",
            properties = {
                    "spring.json.value.default.type=com.smartbank.notification.event.MoneyWithdrawnEvent"
            }
    )
    public void onMoneyWithdrawn(@Payload MoneyWithdrawnEvent event) {
        log.info(" [EVENT] Withdrawal: account={}, amount={}, balanceAfter={}",
                event.getAccountId(), event.getAmount(), event.getBalanceAfter());
    }

    @KafkaListener(
            topics = "money.transferred",
            groupId = "notification-group",
            properties = {
                    "spring.json.value.default.type=com.smartbank.notification.event.MoneyTransferredEvent"
            }
    )
    public void onMoneyTransferred(@Payload MoneyTransferredEvent event) {
        log.info(" [EVENT] Transfer: from={}, to={}, amount={}",
                event.getFromAccountId(), event.getToAccountId(), event.getAmount());
    }
}