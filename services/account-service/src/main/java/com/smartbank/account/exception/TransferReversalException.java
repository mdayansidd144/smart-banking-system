package com.smartbank.account.exception;
public class TransferReversalException extends RuntimeException {
    public TransferReversalException(String message) {
        super(message);
    }
}