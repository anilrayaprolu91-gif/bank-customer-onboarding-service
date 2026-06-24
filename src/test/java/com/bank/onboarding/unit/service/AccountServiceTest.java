package com.bank.onboarding.unit.service;

import com.bank.onboarding.domain.entity.Account;
import com.bank.onboarding.domain.entity.Customer;
import com.bank.onboarding.domain.enums.AccountStatus;
import com.bank.onboarding.domain.enums.AccountType;
import com.bank.onboarding.domain.enums.CustomerStatus;
import com.bank.onboarding.dto.request.AccountCreationRequest;
import com.bank.onboarding.dto.response.AccountResponse;
import com.bank.onboarding.exception.AccountNotFoundException;
import com.bank.onboarding.exception.CustomerNotFoundException;
import com.bank.onboarding.exception.OnboardingException;
import com.bank.onboarding.mapper.AccountMapper;
import com.bank.onboarding.repository.AccountRepository;
import com.bank.onboarding.repository.CustomerRepository;
import com.bank.onboarding.service.impl.AccountServiceImpl;
import com.bank.onboarding.util.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService – Unit Tests")
class AccountServiceTest {

    @Mock AccountRepository  accountRepository;
    @Mock CustomerRepository customerRepository;
    @Mock AccountMapper      accountMapper;

    @InjectMocks AccountServiceImpl accountService;

    // ── createAccount ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createAccount()")
    class CreateAccount {

        @Test
        @DisplayName("should create account for an ACTIVE customer")
        void shouldCreateAccountForActiveCustomer() {
            UUID     customerId = UUID.randomUUID();
            Customer customer   = TestFixtures.buildCustomer();
            customer.setCustomerStatus(CustomerStatus.ACTIVE);

            Account         account  = TestFixtures.buildAccount(customer);
            AccountResponse response = TestFixtures.buildAccountResponse(account.getId(), customerId);

            AccountCreationRequest request = TestFixtures.validAccountCreation().build();

            given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
            given(accountMapper.toAccount(request)).willReturn(account);
            given(accountRepository.save(any(Account.class))).willReturn(account);
            given(accountMapper.toResponse(account)).willReturn(response);

            AccountResponse result = accountService.createAccount(customerId, request);

            assertThat(result).isNotNull();
            assertThat(result.getAccountType()).isEqualTo(AccountType.CHECKING);
            then(accountRepository).should().save(any(Account.class));
        }

        @Test
        @DisplayName("should throw OnboardingException for PENDING_VERIFICATION customer")
        void shouldRejectAccountForPendingCustomer() {
            UUID     customerId = UUID.randomUUID();
            Customer customer   = TestFixtures.buildCustomer();
            customer.setCustomerStatus(CustomerStatus.PENDING_VERIFICATION);

            given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));

            assertThatThrownBy(() -> accountService.createAccount(customerId,
                    TestFixtures.validAccountCreation().build()))
                    .isInstanceOf(OnboardingException.class)
                    .hasMessageContaining("PENDING_VERIFICATION");
        }

        @Test
        @DisplayName("should throw OnboardingException for SUSPENDED customer")
        void shouldRejectAccountForSuspendedCustomer() {
            UUID     customerId = UUID.randomUUID();
            Customer customer   = TestFixtures.buildCustomer();
            customer.setCustomerStatus(CustomerStatus.SUSPENDED);

            given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));

            assertThatThrownBy(() -> accountService.createAccount(customerId,
                    TestFixtures.validAccountCreation().build()))
                    .isInstanceOf(OnboardingException.class)
                    .hasMessageContaining("suspended");
        }

        @Test
        @DisplayName("should throw CustomerNotFoundException when customer does not exist")
        void shouldThrowWhenCustomerNotFound() {
            UUID customerId = UUID.randomUUID();
            given(customerRepository.findById(customerId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.createAccount(customerId,
                    TestFixtures.validAccountCreation().build()))
                    .isInstanceOf(CustomerNotFoundException.class);
        }
    }

    // ── getAccountById ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAccountById()")
    class GetAccountById {

        @Test
        @DisplayName("should return account when found")
        void shouldReturnAccount() {
            UUID      accountId = UUID.randomUUID();
            Customer  customer  = TestFixtures.buildCustomer();
            Account   account   = TestFixtures.buildAccount(customer);
            AccountResponse response = TestFixtures.buildAccountResponse(accountId, customer.getId());

            given(accountRepository.findById(accountId)).willReturn(Optional.of(account));
            given(accountMapper.toResponse(account)).willReturn(response);

            AccountResponse result = accountService.getAccountById(accountId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(accountId);
        }

        @Test
        @DisplayName("should throw AccountNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            UUID id = UUID.randomUUID();
            given(accountRepository.findById(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.getAccountById(id))
                    .isInstanceOf(AccountNotFoundException.class);
        }
    }

    // ── getAccountsByCustomerId ───────────────────────────────────────────────

    @Nested
    @DisplayName("getAccountsByCustomerId()")
    class GetAccountsByCustomerId {

        @Test
        @DisplayName("should return list of accounts for a customer")
        void shouldReturnAccounts() {
            UUID     customerId = UUID.randomUUID();
            Customer customer   = TestFixtures.buildCustomer();
            Account  account    = TestFixtures.buildAccount(customer);

            given(customerRepository.existsById(customerId)).willReturn(true);
            given(accountRepository.findByCustomerId(customerId)).willReturn(List.of(account));
            given(accountMapper.toResponseList(List.of(account)))
                    .willReturn(List.of(TestFixtures.buildAccountResponse(account.getId(), customerId)));

            List<AccountResponse> result = accountService.getAccountsByCustomerId(customerId);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should throw CustomerNotFoundException when customer not found")
        void shouldThrowWhenCustomerNotFound() {
            UUID customerId = UUID.randomUUID();
            given(customerRepository.existsById(customerId)).willReturn(false);

            assertThatThrownBy(() -> accountService.getAccountsByCustomerId(customerId))
                    .isInstanceOf(CustomerNotFoundException.class);
        }
    }

    // ── closeAccount ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("closeAccount()")
    class CloseAccount {

        @Test
        @DisplayName("should close an active account")
        void shouldCloseActiveAccount() {
            UUID      accountId = UUID.randomUUID();
            Customer  customer  = TestFixtures.buildCustomer();
            Account   account   = TestFixtures.buildAccount(customer);
            account.setAccountStatus(AccountStatus.ACTIVE);

            AccountResponse response = TestFixtures.buildAccountResponse(accountId, customer.getId());

            given(accountRepository.findById(accountId)).willReturn(Optional.of(account));
            given(accountRepository.save(account)).willReturn(account);
            given(accountMapper.toResponse(account)).willReturn(response);

            AccountResponse result = accountService.closeAccount(accountId);

            assertThat(account.getAccountStatus()).isEqualTo(AccountStatus.CLOSED);
            assertThat(account.getClosedDate()).isNotNull();
        }

        @Test
        @DisplayName("should throw OnboardingException when account is already closed")
        void shouldThrowWhenAlreadyClosed() {
            UUID      accountId = UUID.randomUUID();
            Customer  customer  = TestFixtures.buildCustomer();
            Account   account   = TestFixtures.buildAccount(customer);
            account.setAccountStatus(AccountStatus.CLOSED);

            given(accountRepository.findById(accountId)).willReturn(Optional.of(account));

            assertThatThrownBy(() -> accountService.closeAccount(accountId))
                    .isInstanceOf(OnboardingException.class)
                    .hasMessageContaining("already closed");
        }
    }
}

