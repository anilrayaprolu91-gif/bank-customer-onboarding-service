package com.bank.onboarding.accounts.repository;

import com.bank.onboarding.accounts.domain.Account;
import com.bank.onboarding.accounts.domain.AccountStatus;
import com.bank.onboarding.accounts.domain.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    List<Account> findByCustomerId(UUID customerId);

    List<Account> findByCustomerIdAndAccountStatus(UUID customerId, AccountStatus accountStatus);

    List<Account> findByCustomerIdAndAccountType(UUID customerId, AccountType accountType);

    @Query("""
            SELECT a FROM Account a
            JOIN FETCH a.customer c
            WHERE c.id = :customerId
            AND a.accountStatus <> 'CLOSED'
            ORDER BY a.openedDate DESC
            """)
    List<Account> findActiveAccountsByCustomerId(@Param("customerId") UUID customerId);

    long countByCustomerId(UUID customerId);
}



