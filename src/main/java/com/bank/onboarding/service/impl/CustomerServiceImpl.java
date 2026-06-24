package com.bank.onboarding.service.impl;

import com.bank.onboarding.domain.entity.*;
import com.bank.onboarding.domain.enums.CustomerStatus;
import com.bank.onboarding.dto.request.*;
import com.bank.onboarding.dto.response.*;
import com.bank.onboarding.exception.*;
import com.bank.onboarding.event.CustomerOnboardedEvent;
import com.bank.onboarding.event.OnboardingEventPublisher;
import com.bank.onboarding.mapper.AccountMapper;
import com.bank.onboarding.mapper.CustomerMapper;
import com.bank.onboarding.repository.CustomerRepository;
import com.bank.onboarding.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    private static final DateTimeFormatter CN_DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final AtomicLong SEQUENCE = new AtomicLong(1);

    private final CustomerRepository customerRepository;
    private final CustomerMapper     customerMapper;
    private final AccountMapper      accountMapper;
    private final OnboardingEventPublisher onboardingEventPublisher;

    // ── Onboarding ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CustomerResponse onboardCustomer(CustomerOnboardingRequest request) {
        PersonalInfoRequest info = request.getPersonalInfo();

        log.info("Starting onboarding for email: {}", info.getEmail());

        // Duplicate checks
        if (customerRepository.existsByEmail(info.getEmail())) {
            throw new DuplicateCustomerException("email", info.getEmail());
        }
        if (info.getTaxIdentificationNumber() != null &&
                customerRepository.existsByTaxIdentificationNumber(info.getTaxIdentificationNumber())) {
            throw new DuplicateCustomerException("taxIdentificationNumber", info.getTaxIdentificationNumber());
        }

        // Validate at least one primary address
        boolean hasPrimary = request.getAddresses().stream()
                .anyMatch(a -> Boolean.TRUE.equals(a.getIsPrimary()));
        if (!hasPrimary) {
            throw new OnboardingException("At least one address must be marked as primary");
        }

        // Build Customer entity
        Customer customer = Customer.builder()
                .customerNumber(generateCustomerNumber())
                .firstName(info.getFirstName())
                .lastName(info.getLastName())
                .email(info.getEmail())
                .phoneNumber(info.getPhoneNumber())
                .dateOfBirth(info.getDateOfBirth())
                .nationality(info.getNationality())
                .taxIdentificationNumber(info.getTaxIdentificationNumber())
                .customerStatus(CustomerStatus.PENDING_VERIFICATION)
                .onboardingDate(LocalDateTime.now())
                .build();

        // Addresses
        for (AddressRequest ar : request.getAddresses()) {
            Address address = customerMapper.toAddress(ar);
            customer.addAddress(address);
        }

        // KYC documents
        for (KycDocumentRequest kd : request.getKycDocuments()) {
            KycDocument doc = customerMapper.toKycDocument(kd);
            customer.addKycDocument(doc);
        }

        // Risk profile
        RiskProfile riskProfile = customerMapper.toRiskProfile(request.getRiskProfile());
        for (RiskFactorRequest rf : request.getRiskProfile().getFactors()) {
            RiskFactor factor = customerMapper.toRiskFactor(rf);
            riskProfile.addRiskFactor(factor);
        }
        customer.setRiskProfile(riskProfile);

        // Optional initial account
        if (request.getInitialAccount() != null) {
            Account account = accountMapper.toAccount(request.getInitialAccount());
            account.setAccountNumber(generateAccountNumber(customer.getCustomerNumber()));
            customer.addAccount(account);
        }

        Customer saved = customerRepository.save(customer);
        log.info("Customer onboarded successfully: id={}, customerNumber={}",
                saved.getId(), saved.getCustomerNumber());

        onboardingEventPublisher.publishCustomerOnboarded(new CustomerOnboardedEvent(
                saved.getId().toString(),
                saved.getCustomerNumber(),
                saved.getEmail(),
                Instant.now()
        ));

        return customerMapper.toResponse(saved);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @Override
    public CustomerResponse getCustomerById(UUID id) {
        Customer customer = customerRepository.findByIdWithAllDetails(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        return customerMapper.toResponse(customer);
    }

    @Override
    public CustomerResponse getCustomerByCustomerNumber(String customerNumber) {
        Customer customer = customerRepository.findByCustomerNumberWithAllDetails(customerNumber)
                .orElseThrow(() -> new CustomerNotFoundException(customerNumber));
        return customerMapper.toResponse(customer);
    }

    @Override
    public Page<CustomerResponse> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable)
                .map(customerMapper::toResponse);
    }

    // ── Status update ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CustomerResponse updateCustomerStatus(UUID id, CustomerStatusUpdateRequest request) {
        Customer customer = customerRepository.findByIdWithAllDetails(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        log.info("Updating customer {} status: {} → {}", id,
                customer.getCustomerStatus(), request.getStatus());

        customer.setCustomerStatus(request.getStatus());
        customer.setStatusReason(request.getReason());

        Customer saved = customerRepository.save(customer);
        return customerMapper.toResponse(saved);
    }

    // ── Onboarding status ─────────────────────────────────────────────────────

    @Override
    public OnboardingStatusResponse getOnboardingStatus(UUID id) {
        Customer customer = customerRepository.findByIdWithAllDetails(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        return customerMapper.toOnboardingStatusResponse(customer);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String generateCustomerNumber() {
        String date = LocalDateTime.now().format(CN_DATE_FMT);
        long seq = SEQUENCE.getAndIncrement();
        return String.format("CUST-%04d-%s", seq, date);
    }

    private String generateAccountNumber(String customerNumber) {
        String ts = String.valueOf(System.currentTimeMillis()).substring(5);
        return "ACC-" + customerNumber.substring(5, 9) + "-" + ts;
    }
}

