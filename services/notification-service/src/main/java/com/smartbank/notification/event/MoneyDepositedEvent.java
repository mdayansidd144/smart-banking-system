package com.smartbank.notification.event;
import java.math.BigDecimal;
import java.util.UUID;
public class MoneyDepositedEvent extends BaseEvent {

    private UUID accountId;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String description;

    public MoneyDepositedEvent() {}

    public MoneyDepositedEvent(UUID accountId, BigDecimal amount,
                               BigDecimal balanceAfter, String description) {
        this.accountId = accountId;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
    }

    public UUID getAccountId() { return accountId; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public String getDescription() { return description; }
}