package com.bank.onboarding.authentication.repository;

import com.bank.onboarding.authentication.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Permission entity.
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    /**
     * Find permission by code.
     */
    Optional<Permission> findByCode(String code);

    /**
     * Check if permission exists by code.
     */
    boolean existsByCode(String code);
}

