package com.smartbank.fraud.event;

import java.math.BigDecimal;
import java.util.UUID;

public class MoneyWithdrawnEvent extends BaseEvent {

    private UUID accountId;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String description;

    public MoneyWithdrawnEvent() {}

    public MoneyWithdrawnEvent(UUID accountId, BigDecimal amount,
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