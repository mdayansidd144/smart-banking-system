package com.smartbank.notification;
import com.smartbank.notification.event.*;
import com.smartbank.notification.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class EventConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventConsumer.class);
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final EmailService emailService;

    public EventConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

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

        emailService.sendEmail(
                "ACCOUNT_CREATED",
                "account-created",
                "Welcome to Smart Bank — Account " + event.getAccountNumber(),
                Map.of(
                        "ownerName", event.getOwnerName(),
                        "accountNumber", event.getAccountNumber(),
                        "currency", event.getCurrency()
                )
        );
    }

    @KafkaListener(
            topics = "money.deposited",
            groupId = "notification-group",
            properties = {
                    "spring.json.value.default.type=com.smartbank.notification.event.MoneyDepositedEvent"
            }
    )
    public void onMoneyDeposited(@Payload MoneyDepositedEvent event) {
        log.info(" [EVENT] Deposit: account={}, amount={}", event.getAccountId(), event.getAmount());

        emailService.sendEmail(
                "DEPOSIT",
                "deposit",
                "Deposit of ₹" + event.getAmount() + " received",
                Map.of(
                        "amount", event.getAmount(),
                        "accountNumber", event.getAccountId().toString().substring(0, 8) + "...",
                        "balanceAfter", event.getBalanceAfter(),
                        "description", event.getDescription() != null ? event.getDescription() : "N/A",
                        "timestamp", LocalDateTime.now().format(FMT)
                )
        );
    }

    @KafkaListener(
            topics = "money.withdrawn",
            groupId = "notification-group",
            properties = {
                    "spring.json.value.default.type=com.smartbank.notification.event.MoneyWithdrawnEvent"
            }
    )
    public void onMoneyWithdrawn(@Payload MoneyWithdrawnEvent event) {
        log.info(" [EVENT] Withdrawal: account={}, amount={}", event.getAccountId(), event.getAmount());

        emailService.sendEmail(
                "WITHDRAWAL",
                "withdrawal",
                "Withdrawal of ₹" + event.getAmount() + " processed",
                Map.of(
                        "amount", event.getAmount(),
                        "accountNumber", event.getAccountId().toString().substring(0, 8) + "...",
                        "balanceAfter", event.getBalanceAfter(),
                        "description", event.getDescription() != null ? event.getDescription() : "N/A",
                        "timestamp", LocalDateTime.now().format(FMT)
                )
        );
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

        emailService.sendEmail(
                "TRANSFER",
                "transfer",
                "Transfer of ₹" + event.getAmount() + " completed",
                Map.of(
                        "amount", event.getAmount(),
                        "fromAccountId", event.getFromAccountId(),
                        "toAccountId", event.getToAccountId(),
                        "description", event.getDescription() != null ? event.getDescription() : "N/A",
                        "timestamp", LocalDateTime.now().format(FMT)
                )
        );
    }
}