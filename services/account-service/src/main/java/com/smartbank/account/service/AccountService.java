package com.smartbank.account.service;

import com.smartbank.account.dto.AccountResponse;
import com.smartbank.account.dto.CreateAccountRequest;
import com.smartbank.account.entity.Account;
import com.smartbank.account.entity.AccountStatus;
import com.smartbank.account.entity.Transaction;
import com.smartbank.account.entity.TransactionType;
import com.smartbank.account.event.AccountCreatedEvent;
import com.smartbank.account.event.AccountFrozenEvent;
import com.smartbank.account.event.EventPublisher;
import com.smartbank.account.event.MoneyDepositedEvent;
import com.smartbank.account.event.MoneyWithdrawnEvent;
import com.smartbank.account.exception.*;
import com.smartbank.account.repository.AccountRepository;
import com.smartbank.account.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository repository;
    private final TransactionRepository transactionRepository;
    private final EventPublisher eventPublisher;
    private final Random random = new Random();

    public AccountService(AccountRepository repository,
                          TransactionRepository transactionRepository,
                          EventPublisher eventPublisher) {
        this.repository = repository;
        this.transactionRepository = transactionRepository;
        this.eventPublisher = eventPublisher;
    }

    // =========================================================
    // Account lifecycle
    // =========================================================

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        Account account = new Account();
        account.setOwnerName(request.getOwnerName());
        account.setCurrency(request.getCurrency().toUpperCase());
        account.setAccountNumber(generateAccountNumber());
        account.setBalance(BigDecimal.ZERO);

        Account saved = repository.save(account);

        eventPublisher.publishAccountCreated(new AccountCreatedEvent(
                saved.getId(),
                saved.getOwnerName(),
                saved.getAccountNumber(),
                saved.getCurrency()
        ));

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(UUID id) {
        Account account = repository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
        return toResponse(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // =========================================================
    // Money operations
    // =========================================================

    @Transactional
    public AccountResponse deposit(UUID accountId, BigDecimal amount, String description) {
        validateAmount(amount);

        Account account = repository.findByIdWithLock(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        ensureActive(account);

        account.setBalance(account.getBalance().add(amount));
        Account saved = repository.save(account);
        recordTransaction(saved, TransactionType.DEPOSIT, amount, description);

        eventPublisher.publishMoneyDeposited(new MoneyDepositedEvent(
                saved.getId(),
                amount,
                saved.getBalance(),
                description
        ));

        return toResponse(saved);
    }

    @Transactional
    public AccountResponse withdraw(UUID accountId, BigDecimal amount, String description) {
        validateAmount(amount);

        Account account = repository.findByIdWithLock(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        ensureActive(account);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(account.getBalance(), amount);
        }

        account.setBalance(account.getBalance().subtract(amount));
        Account saved = repository.save(account);

        recordTransaction(saved, TransactionType.WITHDRAWAL, amount, description);

        eventPublisher.publishMoneyWithdrawn(new MoneyWithdrawnEvent(
                saved.getId(),
                amount,
                saved.getBalance(),
                description
        ));

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactions(UUID accountId) {
        if (!repository.existsById(accountId)) {
            throw new AccountNotFoundException(accountId);
        }
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    @Transactional
    public AccountResponse freezeAccount(UUID accountId, String reason) {
        Account account = repository.findByIdWithLock(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        if (account.getStatus() == AccountStatus.FROZEN) {
            log.info("Account {} already frozen", accountId);
            return toResponse(account);
        }

        account.setStatus(AccountStatus.FROZEN);
        Account saved = repository.save(account);

        log.warn(" Account {} FROZEN — reason: {}", accountId, reason);

        eventPublisher.publishAccountFrozen(new AccountFrozenEvent(
                saved.getId(),
                saved.getAccountNumber(),
                saved.getOwnerName(),
                saved.getBalance(),
                reason
        ));

        return toResponse(saved);
    }

    @Transactional
    public AccountResponse unfreezeAccount(UUID accountId) {
        Account account = repository.findByIdWithLock(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        if (account.getStatus() != AccountStatus.FROZEN) {
            throw new RuntimeException("Account is not frozen");
        }

        account.setStatus(AccountStatus.ACTIVE);
        Account saved = repository.save(account);

        log.info(" Account {} UNFROZEN", accountId);
        return toResponse(saved);
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

    private String generateAccountNumber() {
        String number;
        do {
            number = "SB" + String.format("%010d", random.nextLong(1_000_000_000L));
        } while (repository.existsByAccountNumber(number));
        return number;
    }

    private AccountResponse toResponse(Account a) {
        return new AccountResponse(
                a.getId(),
                a.getOwnerName(),
                a.getAccountNumber(),
                a.getBalance(),
                a.getCurrency(),
                a.getStatus().name(),
                a.getCreatedAt()
        );
    }
}