package com.bank.onboarding.customer.application;

import com.bank.onboarding.customer.domain.Customer;
import com.bank.onboarding.customer.repository.CustomerRepository;
import com.bank.onboarding.dto.request.CustomerStatusUpdateRequest;
import com.bank.onboarding.dto.response.CustomerResponse;
import com.bank.onboarding.dto.response.OnboardingStatusResponse;
import com.bank.onboarding.exception.CustomerNotFoundException;
import com.bank.onboarding.mapper.CustomerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

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

    @Override
    @Transactional
    public CustomerResponse updateCustomerStatus(UUID id, CustomerStatusUpdateRequest request) {
        Customer customer = customerRepository.findByIdWithAllDetails(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        log.info("Updating customer {} status: {} -> {}", id,
                customer.getCustomerStatus(), request.getStatus());

        customer.setCustomerStatus(request.getStatus());
        customer.setStatusReason(request.getReason());

        Customer saved = customerRepository.save(customer);
        return customerMapper.toResponse(saved);
    }

    @Override
    public OnboardingStatusResponse getOnboardingStatus(UUID id) {
        Customer customer = customerRepository.findByIdWithAllDetails(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        return customerMapper.toOnboardingStatusResponse(customer);
    }
}


