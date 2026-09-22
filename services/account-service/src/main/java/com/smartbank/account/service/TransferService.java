package com.smartbank.account.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbank.account.dto.TransferRequest;
import com.smartbank.account.dto.TransferResponse;
import com.smartbank.account.entity.*;
import com.smartbank.account.event.EventPublisher;
import com.smartbank.account.event.MoneyTransferredEvent;
import com.smartbank.account.exception.*;
import com.smartbank.account.repository.AccountRepository;
import com.smartbank.account.repository.IdempotencyRepository;
import com.smartbank.account.repository.TransactionRepository;
import com.smartbank.account.repository.TransferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransferRepository transferRepository;
    private final IdempotencyRepository idempotencyRepository;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public TransferService(AccountRepository accountRepository,
                           TransactionRepository transactionRepository,
                           TransferRepository transferRepository,
                           IdempotencyRepository idempotencyRepository,
                           EventPublisher eventPublisher,
                           ObjectMapper objectMapper) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transferRepository = transferRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public TransferResponse transfer(String idempotencyKey, TransferRequest request) {

        // ---- Idempotency check ----
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<IdempotencyRecord> existing =
                    idempotencyRepository.findByIdempotencyKey(idempotencyKey);

            if (existing.isPresent()) {
                log.info(" Idempotent replay for key: {}", idempotencyKey);
                try {
                    return objectMapper.readValue(
                            existing.get().getResponseJson(),
                            TransferResponse.class
                    );
                } catch (Exception e) {
                    throw new RuntimeException("Failed to deserialize cached response", e);
                }
            }
        }

        // ---- Execute transfer (existing logic) ----
        UUID fromId = request.getFromAccountId();
        UUID toId = request.getToAccountId();
        BigDecimal amount = request.getAmount();

        if (fromId.equals(toId)) {
            throw new SameAccountTransferException(fromId);
        }

        validateAmount(amount);

        UUID firstId  = fromId.compareTo(toId) < 0 ? fromId : toId;
        UUID secondId = fromId.compareTo(toId) < 0 ? toId : fromId;

        Account first  = accountRepository.findByIdWithLock(firstId)
                .orElseThrow(() -> new AccountNotFoundException(firstId));
        Account second = accountRepository.findByIdWithLock(secondId)
                .orElseThrow(() -> new AccountNotFoundException(secondId));

        Account source = fromId.equals(first.getId()) ? first : second;
        Account destination = toId.equals(first.getId()) ? first : second;

        ensureActive(source);
        ensureActive(destination);

        if (source.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(source.getBalance(), amount);
        }

        source.setBalance(source.getBalance().subtract(amount));
        destination.setBalance(destination.getBalance().add(amount));

        accountRepository.save(source);
        accountRepository.save(destination);

        recordTransaction(source, TransactionType.WITHDRAWAL, amount,
                "Transfer out: " + request.getDescription());
        recordTransaction(destination, TransactionType.DEPOSIT, amount,
                "Transfer in: " + request.getDescription());

        Transfer transfer = new Transfer();
        transfer.setFromAccountId(source.getId());
        transfer.setToAccountId(destination.getId());
        transfer.setAmount(amount);
        transfer.setDescription(request.getDescription());
        Transfer savedTransfer = transferRepository.save(transfer);

        eventPublisher.publishMoneyTransferred(new MoneyTransferredEvent(
                savedTransfer.getId(),
                savedTransfer.getFromAccountId(),
                savedTransfer.getToAccountId(),
                savedTransfer.getAmount(),
                savedTransfer.getDescription()
        ));

        TransferResponse response = new TransferResponse(
                savedTransfer.getId(),
                savedTransfer.getFromAccountId(),
                savedTransfer.getToAccountId(),
                savedTransfer.getAmount(),
                savedTransfer.getStatus().name(),
                savedTransfer.getCreatedAt()
        );

        // ---- Store idempotency record ----
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            try {
                IdempotencyRecord record = new IdempotencyRecord();
                record.setIdempotencyKey(idempotencyKey);
                record.setResponseJson(objectMapper.writeValueAsString(response));
                record.setResponseStatus(201);
                idempotencyRepository.save(record);
                log.info(" Cached response for key: {}", idempotencyKey);
            } catch (Exception e) {
                log.warn("Failed to cache idempotency record: {}", e.getMessage());
            }
        }

        return response;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }
        if (amount.scale() > 2) {
            throw new InvalidAmountException("Amount cannot have more than 2 decimal places");
        }
    }

    private void ensureActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(account.getId(), account.getStatus());
        }
    }

    private void recordTransaction(Account account, TransactionType type,
                                   BigDecimal amount, String description) {
        Transaction tx = new Transaction();
        tx.setAccountId(account.getId());
        tx.setType(type);
        tx.setAmount(amount);
        tx.setBalanceAfter(account.getBalance());
        tx.setDescription(description);
        transactionRepository.save(tx);
    }
}