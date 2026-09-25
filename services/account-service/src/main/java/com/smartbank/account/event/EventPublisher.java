package com.smartbank.account.event;
import com.smartbank.account.bill.BillAlertEvent;
import com.smartbank.account.budget.BudgetAlertEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

    public static final String TOPIC_ACCOUNT_CREATED = "account.created";
    public static final String TOPIC_MONEY_DEPOSITED = "money.deposited";
    public static final String TOPIC_MONEY_WITHDRAWN = "money.withdrawn";
    public static final String TOPIC_MONEY_TRANSFERRED = "money.transferred";
    public static final String TOPIC_TRANSFER_REVERSED = "transfer.reversed";
    public static final String TOPIC_STATEMENT_REQUESTED = "statement.requested";
    public static final String TOPIC_BUDGET_ALERT = "budget.alert";
    public static final String TOPIC_BILL_ALERT = "bill.alert";

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

    public void publishTransferReversed(TransferReversedEvent event) {
        kafkaTemplate.send(TOPIC_TRANSFER_REVERSED, event.getTransferId().toString(), event);
    }

    public void publishStatementRequested(StatementRequestedEvent event) {
        kafkaTemplate.send(TOPIC_STATEMENT_REQUESTED, event.getAccountId().toString(), event);
    }

    public void publishBudgetAlert(BudgetAlertEvent event) {
        kafkaTemplate.send(TOPIC_BUDGET_ALERT, event.getBudgetId().toString(), event);
    }

    public void publishBillAlert(BillAlertEvent event) {
        kafkaTemplate.send(TOPIC_BILL_ALERT, event.getBillId().toString(), event);
    }
}