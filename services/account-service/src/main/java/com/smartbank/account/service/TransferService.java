package com.smartbank.account.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbank.account.dto.TransferRequest;
import com.smartbank.account.dto.TransferResponse;
import com.smartbank.account.entity.*;
import com.smartbank.account.event.EventPublisher;
import com.smartbank.account.event.MoneyTransferredEvent;
import com.smartbank.account.event.TransferReversedEvent;
import com.smartbank.account.exception.*;
import com.smartbank.account.fx.FxRateService;
import com.smartbank.account.repository.AccountRepository;
import com.smartbank.account.repository.IdempotencyRepository;
import com.smartbank.account.repository.TransactionRepository;
import com.smartbank.account.repository.TransferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
@Service
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);
    private static final long REVERSAL_WINDOW_HOURS = 24;

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransferRepository transferRepository;
    private final IdempotencyRepository idempotencyRepository;
    private final EventPublisher eventPublisher;
    private final FxRateService fxRateService;
    private final ObjectMapper objectMapper;

    public TransferService(AccountRepository accountRepository,
                           TransactionRepository transactionRepository,
                           TransferRepository transferRepository,
                           IdempotencyRepository idempotencyRepository,
                           EventPublisher eventPublisher,
                           FxRateService fxRateService,
                           ObjectMapper objectMapper) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transferRepository = transferRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.eventPublisher = eventPublisher;
        this.fxRateService = fxRateService;
        this.objectMapper = objectMapper;
    }
    @Transactional
    public TransferResponse transfer(String idempotencyKey, TransferRequest request) {

        // ---- Idempotency replay ----
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<IdempotencyRecord> existing =
                    idempotencyRepository.findByIdempotencyKey(idempotencyKey);

            if (existing.isPresent()) {
                log.info("♻️ Idempotent replay for key: {}", idempotencyKey);
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

        UUID fromId = request.getFromAccountId();
        UUID toId = request.getToAccountId();
        BigDecimal amount = request.getAmount();

        if (fromId.equals(toId)) {
            throw new SameAccountTransferException(fromId);
        }

        validateAmount(amount);

        // Lock accounts in a consistent order to prevent deadlocks
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

        // ---- Determine currencies ----
        String sourceCurrency = source.getCurrency().toUpperCase();
        String targetCurrency = (request.getTargetCurrency() != null
                && !request.getTargetCurrency().isBlank())
                ? request.getTargetCurrency().toUpperCase()
                : destination.getCurrency().toUpperCase();

        // ---- FX conversion ----
        BigDecimal targetAmount;
        BigDecimal fxRate;

        if (sourceCurrency.equals(targetCurrency)) {
            targetAmount = amount;
            fxRate = BigDecimal.ONE;
        } else {
            targetAmount = fxRateService.convert(amount, sourceCurrency, targetCurrency);
            fxRate = targetAmount.divide(amount, 8, RoundingMode.HALF_UP);
            log.info(" FX transfer: {} {} → {} {} (rate {})",
                    amount, sourceCurrency, targetAmount, targetCurrency, fxRate);
        }

        // ---- Balance check (source side) ----
        if (source.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(source.getBalance(), amount);
        }

        // ---- Move the money ----
        source.setBalance(source.getBalance().subtract(amount));
        destination.setBalance(destination.getBalance().add(targetAmount));

        accountRepository.save(source);
        accountRepository.save(destination);

        // ---- Record both transactions ----
        recordTransaction(source, TransactionType.WITHDRAWAL, amount,
                "Transfer out: " + request.getDescription());
        recordTransaction(destination, TransactionType.DEPOSIT, targetAmount,
                "Transfer in: " + request.getDescription()
                        + (sourceCurrency.equals(targetCurrency)
                        ? ""
                        : " (FX rate: " + fxRate + " " + sourceCurrency + "/" + targetCurrency + ")"));

        // ---- Build Transfer entity ----
        Transfer transfer = new Transfer();
        transfer.setFromAccountId(source.getId());
        transfer.setToAccountId(destination.getId());
        transfer.setAmount(amount);
        transfer.setDescription(request.getDescription());
        transfer.setSourceCurrency(sourceCurrency);
        transfer.setTargetCurrency(targetCurrency);
        transfer.setSourceAmount(amount);
        transfer.setTargetAmount(targetAmount);
        transfer.setFxRate(fxRate);

        Transfer savedTransfer = transferRepository.save(transfer);

        // ---- Publish Kafka event ----
        eventPublisher.publishMoneyTransferred(new MoneyTransferredEvent(
                savedTransfer.getId(),
                savedTransfer.getFromAccountId(),
                savedTransfer.getToAccountId(),
                savedTransfer.getAmount(),
                savedTransfer.getDescription()
        ));

        // ---- Build response ----
        TransferResponse response = new TransferResponse(
                savedTransfer.getId(),
                savedTransfer.getFromAccountId(),
                savedTransfer.getToAccountId(),
                savedTransfer.getAmount(),
                savedTransfer.getStatus().name(),
                savedTransfer.getCreatedAt(),
                savedTransfer.getSourceCurrency(),
                savedTransfer.getTargetCurrency(),
                savedTransfer.getSourceAmount(),
                savedTransfer.getTargetAmount(),
                savedTransfer.getFxRate()
        );

        // ---- Cache idempotency ----
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

    @Transactional
    public TransferResponse reverseTransfer(UUID transferId, String reason) {

        Transfer original = transferRepository.findById(transferId)
                .orElseThrow(() -> new TransferReversalException("Transfer not found: " + transferId));

        if (original.getStatus() == TransferStatus.REVERSED) {
            throw new TransferReversalException("Transfer already reversed");
        }

        if (original.getStatus() != TransferStatus.COMPLETED) {
            throw new TransferReversalException("Only COMPLETED transfers can be reversed");
        }

        long hoursSince = ChronoUnit.HOURS.between(original.getCreatedAt(), LocalDateTime.now());
        if (hoursSince > REVERSAL_WINDOW_HOURS) {
            throw new TransferReversalException(
                    "Reversal window expired. Only transfers within "
                            + REVERSAL_WINDOW_HOURS + " hours can be reversed.");
        }

        UUID fromId = original.getFromAccountId();
        UUID toId = original.getToAccountId();

        // Amount returned to source = original sourceAmount (in source currency)
        BigDecimal sourceAmount = original.getSourceAmount() != null
                ? original.getSourceAmount() : original.getAmount();

        // Amount taken from destination = original targetAmount (in target currency)
        BigDecimal targetAmount = original.getTargetAmount() != null
                ? original.getTargetAmount() : original.getAmount();

        UUID firstId  = fromId.compareTo(toId) < 0 ? fromId : toId;
        UUID secondId = fromId.compareTo(toId) < 0 ? toId : fromId;

        Account first  = accountRepository.findByIdWithLock(firstId)
                .orElseThrow(() -> new AccountNotFoundException(firstId));
        Account second = accountRepository.findByIdWithLock(secondId)
                .orElseThrow(() -> new AccountNotFoundException(secondId));

        Account originalSource = fromId.equals(first.getId()) ? first : second;
        Account originalDestination = toId.equals(first.getId()) ? first : second;

        // Destination must have the targetAmount to send back
        if (originalDestination.getBalance().compareTo(targetAmount) < 0) {
            throw new InsufficientFundsException(originalDestination.getBalance(), targetAmount);
        }

        // Reverse money
        originalDestination.setBalance(originalDestination.getBalance().subtract(targetAmount));
        originalSource.setBalance(originalSource.getBalance().add(sourceAmount));

        accountRepository.save(originalDestination);
        accountRepository.save(originalSource);

        recordTransaction(originalDestination, TransactionType.WITHDRAWAL, targetAmount,
                "Reversal out: " + reason);
        recordTransaction(originalSource, TransactionType.DEPOSIT, sourceAmount,
                "Reversal in: " + reason);

        original.setStatus(TransferStatus.REVERSED);
        original.setReversedAt(LocalDateTime.now());
        original.setReversalReason(reason);
        Transfer savedOriginal = transferRepository.save(original);

        eventPublisher.publishTransferReversed(new TransferReversedEvent(
                savedOriginal.getId(),
                savedOriginal.getFromAccountId(),
                savedOriginal.getToAccountId(),
                savedOriginal.getAmount(),
                reason
        ));

        return new TransferResponse(
                savedOriginal.getId(),
                savedOriginal.getFromAccountId(),
                savedOriginal.getToAccountId(),
                savedOriginal.getAmount(),
                savedOriginal.getStatus().name(),
                savedOriginal.getCreatedAt(),
                savedOriginal.getSourceCurrency(),
                savedOriginal.getTargetCurrency(),
                savedOriginal.getSourceAmount(),
                savedOriginal.getTargetAmount(),
                savedOriginal.getFxRate()
        );
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