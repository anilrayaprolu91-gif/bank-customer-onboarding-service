package com.bank.onboarding.onboarding.application;

import com.bank.onboarding.accounts.domain.Account;
import com.bank.onboarding.customer.domain.Address;
import com.bank.onboarding.customer.domain.Customer;
import com.bank.onboarding.customer.domain.CustomerStatus;
import com.bank.onboarding.customer.repository.CustomerRepository;
import com.bank.onboarding.dto.request.AddressRequest;
import com.bank.onboarding.dto.request.CustomerOnboardingRequest;
import com.bank.onboarding.dto.request.KycDocumentRequest;
import com.bank.onboarding.dto.request.PersonalInfoRequest;
import com.bank.onboarding.dto.request.RiskFactorRequest;
import com.bank.onboarding.dto.response.CustomerResponse;
import com.bank.onboarding.event.CustomerOnboardedEvent;
import com.bank.onboarding.event.OnboardingEventPublisher;
import com.bank.onboarding.exception.DuplicateCustomerException;
import com.bank.onboarding.exception.OnboardingException;
import com.bank.onboarding.kyc.domain.KycDocument;
import com.bank.onboarding.mapper.AccountMapper;
import com.bank.onboarding.mapper.CustomerMapper;
import com.bank.onboarding.risk.domain.RiskFactor;
import com.bank.onboarding.risk.domain.RiskProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomerOnboardingServiceImpl implements CustomerOnboardingService {

    private static final DateTimeFormatter CN_DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final AtomicLong SEQUENCE = new AtomicLong(1);

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final AccountMapper accountMapper;
    private final OnboardingEventPublisher onboardingEventPublisher;

    @Override
    @Transactional
    public CustomerResponse onboardCustomer(CustomerOnboardingRequest request) {
        PersonalInfoRequest info = request.getPersonalInfo();

        log.info("Starting onboarding for email: {}", info.getEmail());

        if (customerRepository.existsByEmail(info.getEmail())) {
            throw new DuplicateCustomerException("email", info.getEmail());
        }
        if (info.getTaxIdentificationNumber() != null
                && customerRepository.existsByTaxIdentificationNumber(info.getTaxIdentificationNumber())) {
            throw new DuplicateCustomerException("taxIdentificationNumber", info.getTaxIdentificationNumber());
        }

        boolean hasPrimary = request.getAddresses().stream()
                .anyMatch(a -> Boolean.TRUE.equals(a.getIsPrimary()));
        if (!hasPrimary) {
            throw new OnboardingException("At least one address must be marked as primary");
        }

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

        for (AddressRequest ar : request.getAddresses()) {
            Address address = customerMapper.toAddress(ar);
            customer.addAddress(address);
        }

        for (KycDocumentRequest kd : request.getKycDocuments()) {
            KycDocument doc = customerMapper.toKycDocument(kd);
            customer.addKycDocument(doc);
        }

        RiskProfile riskProfile = customerMapper.toRiskProfile(request.getRiskProfile());
        for (RiskFactorRequest rf : request.getRiskProfile().getFactors()) {
            RiskFactor factor = customerMapper.toRiskFactor(rf);
            riskProfile.addRiskFactor(factor);
        }
        customer.setRiskProfile(riskProfile);

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

