package com.smartbank.account.service;
import com.smartbank.account.dto.AccountResponse;
import com.smartbank.account.dto.CreateAccountRequest;
import com.smartbank.account.entity.Account;
import com.smartbank.account.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
public class AccountService {
    private final AccountRepository repository;
    private final Random random = new Random();

    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        Account account = new Account();
        account.setOwnerName(request.getOwnerName());
        account.setCurrency(request.getCurrency().toUpperCase());
        account.setAccountNumber(generateAccountNumber());
        account.setBalance(BigDecimal.ZERO);
        Account saved = repository.save(account);
        return toResponse(saved);
    }
    @Transactional(readOnly = true)
    public AccountResponse getAccount(UUID id) {
        Account account = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found: " + id));
        return toResponse(account);
    }
    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
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