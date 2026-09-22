package com.smartbank.account.exception;
import java.util.UUID;
public class SameAccountTransferException extends RuntimeException {
    public SameAccountTransferException(UUID id) {
        super("Cannot transfer to the same account: " + id);
    }
}