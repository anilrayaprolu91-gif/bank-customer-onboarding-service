package com.bank.onboarding.accounts.application;

import com.bank.onboarding.accounts.domain.Account;
import com.bank.onboarding.accounts.domain.AccountStatus;
import com.bank.onboarding.customer.domain.Customer;
import com.bank.onboarding.dto.request.AccountCreationRequest;
import com.bank.onboarding.dto.response.AccountResponse;
import com.bank.onboarding.customer.repository.CustomerRepository;
import com.bank.onboarding.exception.AccountNotFoundException;
import com.bank.onboarding.exception.CustomerNotFoundException;
import com.bank.onboarding.exception.OnboardingException;
import com.bank.onboarding.mapper.AccountMapper;
import com.bank.onboarding.accounts.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final AccountRepository  accountRepository;
    private final CustomerRepository customerRepository;
    private final AccountMapper      accountMapper;

    @Override
    @Transactional
    public AccountResponse createAccount(UUID customerId, AccountCreationRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        switch (customer.getCustomerStatus()) {
            case PENDING_VERIFICATION, UNDER_REVIEW ->
                throw new OnboardingException("Cannot open an account for a customer in status: "
                        + customer.getCustomerStatus() + ". Customer must be ACTIVE.");
            case SUSPENDED ->
                throw new OnboardingException("Cannot open an account for a suspended customer.");
            case CLOSED, REJECTED ->
                throw new OnboardingException("Cannot open an account for a closed or rejected customer.");
            default -> { /* ACTIVE â€“ proceed */ }
        }

        Account account = accountMapper.toAccount(request);
        account.setAccountNumber(generateAccountNumber(customer.getCustomerNumber()));
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setOpenedDate(LocalDateTime.now());
        account.setCustomer(customer);

        Account saved = accountRepository.save(account);
        log.info("Account {} created for customer {}", saved.getAccountNumber(), customerId);
        return accountMapper.toResponse(saved);
    }

    @Override
    public AccountResponse getAccountById(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        return accountMapper.toResponse(account);
    }

    @Override
    public List<AccountResponse> getAccountsByCustomerId(UUID customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }
        return accountMapper.toResponseList(accountRepository.findByCustomerId(customerId));
    }

    @Override
    @Transactional
    public AccountResponse closeAccount(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        if (account.getAccountStatus() == AccountStatus.CLOSED) {
            throw new OnboardingException("Account " + account.getAccountNumber() + " is already closed.");
        }

        account.setAccountStatus(AccountStatus.CLOSED);
        account.setClosedDate(LocalDateTime.now());
        Account saved = accountRepository.save(account);
        log.info("Account {} closed", saved.getAccountNumber());
        return accountMapper.toResponse(saved);
    }

    // â”€â”€ Helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private String generateAccountNumber(String customerNumber) {
        String seq = String.valueOf(System.currentTimeMillis()).substring(7);
        return "ACC-" + customerNumber.substring(5, 9) + "-" + seq;
    }
}





