package com.smartbank.account;

import com.smartbank.account.dto.AccountResponse;
import com.smartbank.account.dto.CreateAccountRequest;
import com.smartbank.account.entity.Account;
import com.smartbank.account.entity.AccountStatus;
import com.smartbank.account.exception.InsufficientFundsException;
import com.smartbank.account.exception.InvalidAmountException;
import com.smartbank.account.repository.AccountRepository;
import com.smartbank.account.repository.TransactionRepository;
import com.smartbank.account.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository repository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountService service;

    @Test
    void shouldCreateAccountWithZeroBalance() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setOwnerName("Test User");
        request.setCurrency("USD");

        Account saved = new Account();
        saved.setId(UUID.randomUUID());
        saved.setOwnerName("Test User");
        saved.setAccountNumber("SB0000000001");
        saved.setCurrency("USD");
        saved.setBalance(BigDecimal.ZERO);
        saved.setStatus(AccountStatus.ACTIVE);

        when(repository.existsByAccountNumber(any())).thenReturn(false);
        when(repository.save(any(Account.class))).thenReturn(saved);

        AccountResponse response = service.createAccount(request);

        assertThat(response).isNotNull();
        assertThat(response.getOwnerName()).isEqualTo("Test User");
        assertThat(response.getCurrency()).isEqualTo("USD");
    }

    @Test
    void shouldIncreaseBalanceOnDeposit() {
        UUID id = UUID.randomUUID();
        Account account = activeAccount(id, new BigDecimal("100.00"));

        when(repository.findByIdWithLock(id)).thenReturn(Optional.of(account));
        when(repository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountResponse response = service.deposit(id, new BigDecimal("50.00"), "test");

        assertThat(response.getBalance()).isEqualByComparingTo("150.00");
    }

    @Test
    void shouldReduceBalanceOnWithdraw() {
        UUID id = UUID.randomUUID();
        Account account = activeAccount(id, new BigDecimal("100.00"));

        when(repository.findByIdWithLock(id)).thenReturn(Optional.of(account));
        when(repository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountResponse response = service.withdraw(id, new BigDecimal("30.00"), "test");

        assertThat(response.getBalance()).isEqualByComparingTo("70.00");
    }

    @Test
    void shouldThrowWhenWithdrawExceedsBalance() {
        UUID id = UUID.randomUUID();
        Account account = activeAccount(id, new BigDecimal("50.00"));

        when(repository.findByIdWithLock(id)).thenReturn(Optional.of(account));

        assertThrows(
                InsufficientFundsException.class,
                () -> service.withdraw(id, new BigDecimal("100.00"), "test")
        );
    }

    @Test
    void shouldThrowOnNegativeAmount() {
        UUID id = UUID.randomUUID();

        assertThrows(
                InvalidAmountException.class,
                () -> service.deposit(id, new BigDecimal("-50.00"), "test")
        );
    }

    private Account activeAccount(UUID id, BigDecimal balance) {
        Account a = new Account();
        a.setId(id);
        a.setOwnerName("Test User");
        a.setAccountNumber("SB0000000001");
        a.setCurrency("USD");
        a.setBalance(balance);
        a.setStatus(AccountStatus.ACTIVE);
        return a;
    }
}