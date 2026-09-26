package com.smartbank.notification;
import com.smartbank.notification.event.*;
import com.smartbank.notification.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventConsumer.class);
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
    private static final BigDecimal URGENT_THRESHOLD = new BigDecimal("50000");

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

        Map<String, Object> vars = new HashMap<>();
        vars.put("ownerName", event.getOwnerName());
        vars.put("accountNumber", event.getAccountNumber());
        vars.put("currency", event.getCurrency());

        emailService.sendEmail(
                "ACCOUNT_CREATED",
                "account-created",
                "Welcome to Smart Bank - Account " + event.getAccountNumber(),
                vars
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
        log.info("[EVENT] Deposit: account={}, amount={}",
                event.getAccountId(), event.getAmount());

        boolean urgent = event.getAmount().compareTo(URGENT_THRESHOLD) >= 0;
        String description = event.getDescription() != null
                ? event.getDescription() : "N/A";

        Map<String, Object> vars = new HashMap<>();
        vars.put("amount", event.getAmount());
        vars.put("accountNumber", event.getAccountId().toString().substring(0, 8) + "...");
        vars.put("balanceAfter", event.getBalanceAfter());
        vars.put("description", description);
        vars.put("timestamp", LocalDateTime.now().format(FMT));
        vars.put("urgent", urgent);

        emailService.sendEmailWithPriority(
                "DEPOSIT",
                "deposit",
                "Deposit of Rs. " + event.getAmount() + " received",
                urgent,
                vars
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
        log.info(" [EVENT] Withdrawal: account={}, amount={}",
                event.getAccountId(), event.getAmount());

        boolean urgent = event.getAmount().compareTo(URGENT_THRESHOLD) >= 0;
        String description = event.getDescription() != null
                ? event.getDescription() : "N/A";

        Map<String, Object> vars = new HashMap<>();
        vars.put("amount", event.getAmount());
        vars.put("accountNumber", event.getAccountId().toString().substring(0, 8) + "...");
        vars.put("balanceAfter", event.getBalanceAfter());
        vars.put("description", description);
        vars.put("timestamp", LocalDateTime.now().format(FMT));
        vars.put("urgent", urgent);

        emailService.sendEmailWithPriority(
                "WITHDRAWAL",
                "withdrawal",
                "Withdrawal of Rs. " + event.getAmount() + " processed",
                urgent,
                vars
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

        boolean urgent = event.getAmount().compareTo(URGENT_THRESHOLD) >= 0;
        String description = event.getDescription() != null
                ? event.getDescription() : "N/A";

        Map<String, Object> vars = new HashMap<>();
        vars.put("amount", event.getAmount());
        vars.put("fromAccountId", event.getFromAccountId());
        vars.put("toAccountId", event.getToAccountId());
        vars.put("description", description);
        vars.put("timestamp", LocalDateTime.now().format(FMT));
        vars.put("urgent", urgent);

        emailService.sendEmailWithPriority(
                "TRANSFER",
                "transfer",
                "Transfer of Rs. " + event.getAmount() + " completed",
                urgent,
                vars
        );
    }

    @KafkaListener(
            topics = "statement.requested",
            groupId = "notification-group",
            properties = {
                    "spring.json.value.default.type=com.smartbank.notification.event.StatementRequestedEvent"
            }
    )
    public void onStatementRequested(@Payload StatementRequestedEvent event) {
        log.info(" [EVENT] Statement requested: account={}, recipient={}, from={}, to={}",
                event.getAccountId(), event.getRecipientEmail(),
                event.getFromDate(), event.getToDate());

        try {
            emailService.sendStatementEmail(event);
        } catch (Exception e) {
            log.error("Failed to send statement email: {}", e.getMessage(), e);
        }
    }
    @KafkaListener(
            topics = "budget.alert",
            groupId = "notification-group",
            properties = {
                    "spring.json.value.default.type=com.smartbank.notification.event.BudgetAlertEvent"
            }
    )
    public void onBudgetAlert(@Payload BudgetAlertEvent event) {
        log.info(" [EVENT] Budget alert: category={}, status={}, used={}%",
                event.getCategory(), event.getStatus(), event.getPercentageUsed());

        try {
            String subject = "EXCEEDED".equals(event.getStatus())
                    ? "Budget EXCEEDED for " + event.getCategory()
                    : "Budget warning for " + event.getCategory();

            Map<String, Object> vars = new HashMap<>();
            vars.put("category", event.getCategory());
            vars.put("monthlyLimit", event.getMonthlyLimit());
            vars.put("currentSpend", event.getCurrentSpend());
            vars.put("percentageUsed", event.getPercentageUsed());
            vars.put("status", event.getStatus());
            vars.put("timestamp", LocalDateTime.now().format(FMT));

            boolean urgent = "EXCEEDED".equals(event.getStatus());

            emailService.sendEmailWithPriority(
                    "BUDGET_ALERT",
                    "budget-alert",
                    subject,
                    urgent,
                    vars
            );
        } catch (Exception e) {
            log.error("Failed to send budget alert email: {}", e.getMessage(), e);
        }
    }
    @KafkaListener(
            topics = "bill.alert",
            groupId = "notification-group",
            properties = {
                    "spring.json.value.default.type=com.smartbank.notification.event.BillAlertEvent"
            }
    )
    public void onBillAlert(@Payload BillAlertEvent event) {
        log.info(" [EVENT] Bill alert: biller={}, type={}, amount={}",
                event.getBillerName(), event.getAlertType(), event.getAmount());

        try {
            String subject;
            boolean urgent;
            switch (event.getAlertType()) {
                case "REMINDER":
                    subject = "Reminder: " + event.getBillerName() + " bill due soon";
                    urgent = false;
                    break;
                case "PAID":
                    subject = "Bill paid: " + event.getBillerName();
                    urgent = false;
                    break;
                case "FAILED":
                    subject = "Bill payment FAILED: " + event.getBillerName();
                    urgent = true;
                    break;
                default:
                    subject = "Bill alert: " + event.getBillerName();
                    urgent = false;
            }

            Map<String, Object> vars = new HashMap<>();
            vars.put("billId", event.getBillId());
            vars.put("billerName", event.getBillerName());
            vars.put("category", event.getCategory());
            vars.put("accountNumber", event.getAccountNumber());
            vars.put("amount", event.getAmount());
            vars.put("dueDate", event.getDueDate());
            vars.put("alertType", event.getAlertType());
            vars.put("timestamp", LocalDateTime.now().format(FMT));

            emailService.sendEmailWithPriority(
                    "BILL_ALERT",
                    "bill-alert",
                    subject,
                    urgent,
                    vars
            );
        } catch (Exception e) {
            log.error("Failed to send bill alert email: {}", e.getMessage(), e);
        }
    }
    @KafkaListener(
            topics = "account.frozen",
            groupId = "notification-group",
            properties = {
                    "spring.json.value.default.type=com.smartbank.notification.event.AccountFrozenEvent"
            }
    )
    public void onAccountFrozen(@Payload AccountFrozenEvent event) {
        log.warn(" [EVENT] Account frozen: {} ({}), reason={}",
                event.getAccountNumber(), event.getAccountId(), event.getReason());

        try {
            Map<String, Object> vars = new HashMap<>();
            vars.put("accountNumber", event.getAccountNumber());
            vars.put("ownerName", event.getOwnerName());
            vars.put("balance", event.getBalance());
            vars.put("reason", event.getReason());
            vars.put("timestamp", LocalDateTime.now().format(FMT));

            emailService.sendEmailWithPriority(
                    "ACCOUNT_FROZEN",
                    "account-frozen",
                    "URGENT: Your account " + event.getAccountNumber() + " has been frozen",
                    true,
                    vars
            );
        } catch (Exception e) {
            log.error("Failed to send freeze email: {}", e.getMessage(), e);
        }
    }
}