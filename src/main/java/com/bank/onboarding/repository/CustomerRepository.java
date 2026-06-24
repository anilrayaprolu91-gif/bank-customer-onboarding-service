package com.bank.onboarding.repository;

import com.bank.onboarding.domain.entity.Customer;
import com.bank.onboarding.domain.enums.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID>, JpaSpecificationExecutor<Customer> {

    Optional<Customer> findByCustomerNumber(String customerNumber);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByTaxIdentificationNumber(String taxIdentificationNumber);

    boolean existsByEmail(String email);

    boolean existsByTaxIdentificationNumber(String taxIdentificationNumber);

    Page<Customer> findByCustomerStatus(CustomerStatus customerStatus, Pageable pageable);

    @Query("""
            SELECT c FROM Customer c
            LEFT JOIN FETCH c.riskProfile rp
            LEFT JOIN FETCH rp.riskFactors
            WHERE c.id = :id
            """)
    Optional<Customer> findByIdWithAllDetails(@Param("id") UUID id);

    @Query("""
            SELECT c FROM Customer c
            LEFT JOIN FETCH c.riskProfile rp
            LEFT JOIN FETCH rp.riskFactors
            WHERE c.customerNumber = :customerNumber
            """)
    Optional<Customer> findByCustomerNumberWithAllDetails(@Param("customerNumber") String customerNumber);

    @Query("""
            SELECT COUNT(c) FROM Customer c
            WHERE c.customerStatus = :status
            AND c.onboardingDate >= :since
            """)
    long countByStatusSince(@Param("status") CustomerStatus status,
                            @Param("since") LocalDateTime since);

    @Modifying
    @Query("UPDATE Customer c SET c.customerStatus = :status, c.statusReason = :reason WHERE c.id = :id")
    int updateStatus(@Param("id") UUID id,
                     @Param("status") CustomerStatus status,
                     @Param("reason") String reason);
}

