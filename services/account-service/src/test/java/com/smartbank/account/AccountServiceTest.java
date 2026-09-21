package com.smartbank.account;

import com.smartbank.account.dto.AccountResponse;
import com.smartbank.account.dto.CreateAccountRequest;
import com.smartbank.account.entity.Account;
import com.smartbank.account.entity.AccountStatus;
import com.smartbank.account.repository.AccountRepository;
import com.smartbank.account.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository repository;

    @InjectMocks
    private AccountService service;

    @Test
    void shouldCreateAccountWithZeroBalance() {
        // Arrange
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

        // Act
        AccountResponse response = service.createAccount(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getOwnerName()).isEqualTo("Test User");
        assertThat(response.getCurrency()).isEqualTo("USD");
    }
}