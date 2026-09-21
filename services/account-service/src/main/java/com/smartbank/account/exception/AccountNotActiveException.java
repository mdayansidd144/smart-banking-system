package com.smartbank.account.exception;

import com.smartbank.account.entity.AccountStatus;
import java.util.UUID;
public class AccountNotActiveException extends RuntimeException {
    public AccountNotActiveException(UUID id, AccountStatus status) {
        super("Account " + id + " is not active. Current status: " + status);
    }
}