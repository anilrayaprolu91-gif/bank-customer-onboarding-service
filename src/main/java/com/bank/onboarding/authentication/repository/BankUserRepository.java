package com.bank.onboarding.authentication.repository;

import com.bank.onboarding.authentication.domain.BankUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for BankUser entity with optimized queries.
 */
@Repository
public interface BankUserRepository extends JpaRepository<BankUser, UUID> {

    /**
     * Find user by username with roles eager-loaded.
     */
    @Query("SELECT u FROM BankUser u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<BankUser> findByUsernameWithRoles(String username);

    /**
     * Find user by email.
     */
    Optional<BankUser> findByEmail(String email);

    /**
     * Check if username exists.
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists.
     */
    boolean existsByEmail(String email);
}

