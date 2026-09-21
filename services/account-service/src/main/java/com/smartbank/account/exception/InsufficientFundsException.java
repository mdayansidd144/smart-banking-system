package com.smartbank.account.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(BigDecimal balance, BigDecimal requested) {
        super(String.format(
                "Insufficient funds. Balance: %s, Requested: %s",
                balance, requested
        ));
    }
}