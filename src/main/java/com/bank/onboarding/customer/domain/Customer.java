package com.bank.onboarding.customer.domain;

import com.bank.onboarding.accounts.domain.Account;
import com.bank.onboarding.kyc.domain.KycDocument;
import com.bank.onboarding.risk.domain.RiskProfile;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "customers",
    indexes = {
        @Index(name = "idx_customers_email",           columnList = "email"),
        @Index(name = "idx_customers_customer_number", columnList = "customer_number"),
        @Index(name = "idx_customers_status",          columnList = "customer_status")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"addresses", "kycDocuments", "riskProfile", "accounts"})
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "customer_number", nullable = false, unique = true, length = 20)
    private String customerNumber;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "nationality", nullable = false, length = 3)
    private String nationality;

    @Column(name = "tax_identification_number", unique = true, length = 50)
    private String taxIdentificationNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_status", nullable = false, length = 30)
    @Builder.Default
    private CustomerStatus customerStatus = CustomerStatus.PENDING_VERIFICATION;

    @Column(name = "onboarding_date", nullable = false)
    private LocalDateTime onboardingDate;

    @Column(name = "status_reason", length = 500)
    private String statusReason;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<KycDocument> kycDocuments = new ArrayList<>();

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private RiskProfile riskProfile;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Account> accounts = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // â”€â”€ Convenience mutators â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public void addAddress(Address address) {
        addresses.add(address);
        address.setCustomer(this);
    }

    public void addKycDocument(KycDocument document) {
        kycDocuments.add(document);
        document.setCustomer(this);
    }

    public void setRiskProfile(RiskProfile riskProfile) {
        this.riskProfile = riskProfile;
        if (riskProfile != null) {
            riskProfile.setCustomer(this);
        }
    }

    public void addAccount(Account account) {
        accounts.add(account);
        account.setCustomer(this);
    }
}



