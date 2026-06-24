package com.bank.onboarding.unit.service;

import com.bank.onboarding.domain.entity.Customer;
import com.bank.onboarding.domain.enums.CustomerStatus;
import com.bank.onboarding.dto.request.CustomerOnboardingRequest;
import com.bank.onboarding.dto.request.CustomerStatusUpdateRequest;
import com.bank.onboarding.dto.response.CustomerResponse;
import com.bank.onboarding.dto.response.OnboardingStatusResponse;
import com.bank.onboarding.exception.CustomerNotFoundException;
import com.bank.onboarding.exception.DuplicateCustomerException;
import com.bank.onboarding.exception.OnboardingException;
import com.bank.onboarding.event.OnboardingEventPublisher;
import com.bank.onboarding.mapper.AccountMapper;
import com.bank.onboarding.mapper.CustomerMapper;
import com.bank.onboarding.repository.CustomerRepository;
import com.bank.onboarding.service.impl.CustomerServiceImpl;
import com.bank.onboarding.util.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService – Unit Tests")
class CustomerServiceTest {

    @Mock CustomerRepository customerRepository;
    @Mock CustomerMapper     customerMapper;
    @Mock AccountMapper      accountMapper;
    @Mock OnboardingEventPublisher onboardingEventPublisher;

    @InjectMocks CustomerServiceImpl customerService;

    // ── onboardCustomer ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("onboardCustomer()")
    class OnboardCustomer {

        @Test
        @DisplayName("should successfully onboard a new customer")
        void shouldOnboardNewCustomer() {
            // given
            CustomerOnboardingRequest request  = TestFixtures.validOnboardingRequest().build();
            Customer                  customer = TestFixtures.buildCustomer();
            CustomerResponse          response = TestFixtures.buildCustomerResponse(customer.getId());

            given(customerRepository.existsByEmail(anyString())).willReturn(false);
            given(customerRepository.existsByTaxIdentificationNumber(anyString())).willReturn(false);
            given(customerMapper.toAddress(any())).willReturn(TestFixtures.buildAddress(customer));
            given(customerMapper.toKycDocument(any())).willReturn(TestFixtures.buildKycDocument(customer));
            given(customerMapper.toRiskProfile(any())).willReturn(TestFixtures.buildRiskProfile(customer));
            given(customerMapper.toRiskFactor(any())).willReturn(TestFixtures.buildRiskProfile(customer).getRiskFactors().get(0));
            given(accountMapper.toAccount(any())).willReturn(TestFixtures.buildAccount(customer));
            given(customerRepository.save(any(Customer.class))).willReturn(customer);
            given(customerMapper.toResponse(customer)).willReturn(response);

            // when
            CustomerResponse result = customerService.onboardCustomer(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(customer.getId());
            then(customerRepository).should().save(any(Customer.class));
            then(onboardingEventPublisher).should().publishCustomerOnboarded(any());
        }

        @Test
        @DisplayName("should throw DuplicateCustomerException when email already exists")
        void shouldThrowWhenEmailDuplicate() {
            // given
            CustomerOnboardingRequest request = TestFixtures.validOnboardingRequest().build();
            given(customerRepository.existsByEmail(anyString())).willReturn(true);

            // when / then
            assertThatThrownBy(() -> customerService.onboardCustomer(request))
                    .isInstanceOf(DuplicateCustomerException.class)
                    .hasMessageContaining("email");

            then(customerRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("should throw DuplicateCustomerException when TaxId already exists")
        void shouldThrowWhenTaxIdDuplicate() {
            // given
            CustomerOnboardingRequest request = TestFixtures.validOnboardingRequest().build();
            given(customerRepository.existsByEmail(anyString())).willReturn(false);
            given(customerRepository.existsByTaxIdentificationNumber(anyString())).willReturn(true);

            // when / then
            assertThatThrownBy(() -> customerService.onboardCustomer(request))
                    .isInstanceOf(DuplicateCustomerException.class)
                    .hasMessageContaining("taxIdentificationNumber");
        }

        @Test
        @DisplayName("should throw OnboardingException when no primary address provided")
        void shouldThrowWhenNoPrimaryAddress() {
            // given – override address to isPrimary=false
            CustomerOnboardingRequest request = TestFixtures.validOnboardingRequest()
                    .addresses(List.of(TestFixtures.validAddress().isPrimary(false).build()))
                    .build();

            given(customerRepository.existsByEmail(anyString())).willReturn(false);
            given(customerRepository.existsByTaxIdentificationNumber(anyString())).willReturn(false);

            // when / then
            assertThatThrownBy(() -> customerService.onboardCustomer(request))
                    .isInstanceOf(OnboardingException.class)
                    .hasMessageContaining("primary");
        }
    }

    // ── getCustomerById ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("getCustomerById()")
    class GetCustomerById {

        @Test
        @DisplayName("should return customer when found")
        void shouldReturnCustomerWhenFound() {
            // given
            UUID     id       = UUID.randomUUID();
            Customer customer = TestFixtures.buildCustomer();
            CustomerResponse response = TestFixtures.buildCustomerResponse(id);

            given(customerRepository.findByIdWithAllDetails(id)).willReturn(Optional.of(customer));
            given(customerMapper.toResponse(customer)).willReturn(response);

            // when
            CustomerResponse result = customerService.getCustomerById(id);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("should throw CustomerNotFoundException when customer does not exist")
        void shouldThrowWhenNotFound() {
            UUID id = UUID.randomUUID();
            given(customerRepository.findByIdWithAllDetails(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> customerService.getCustomerById(id))
                    .isInstanceOf(CustomerNotFoundException.class)
                    .hasMessageContaining(id.toString());
        }
    }

    // ── getAllCustomers ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAllCustomers()")
    class GetAllCustomers {

        @Test
        @DisplayName("should return paginated customer list")
        void shouldReturnPaginatedList() {
            // given
            Customer         customer  = TestFixtures.buildCustomer();
            CustomerResponse response  = TestFixtures.buildCustomerResponse(customer.getId());
            PageRequest      pageable  = PageRequest.of(0, 10);
            Page<Customer>   page      = new PageImpl<>(List.of(customer), pageable, 1);

            given(customerRepository.findAll(pageable)).willReturn(page);
            given(customerMapper.toResponse(customer)).willReturn(response);

            // when
            Page<CustomerResponse> result = customerService.getAllCustomers(pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);
        }
    }

    // ── updateCustomerStatus ──────────────────────────────────────────────────

    @Nested
    @DisplayName("updateCustomerStatus()")
    class UpdateCustomerStatus {

        @Test
        @DisplayName("should update status successfully")
        void shouldUpdateStatus() {
            // given
            UUID     id       = UUID.randomUUID();
            Customer customer = TestFixtures.buildCustomer();
            CustomerStatusUpdateRequest statusRequest = CustomerStatusUpdateRequest.builder()
                    .status(CustomerStatus.ACTIVE)
                    .reason("All documents verified")
                    .build();
            CustomerResponse response = TestFixtures.buildCustomerResponse(id);

            given(customerRepository.findByIdWithAllDetails(id)).willReturn(Optional.of(customer));
            given(customerRepository.save(customer)).willReturn(customer);
            given(customerMapper.toResponse(customer)).willReturn(response);

            // when
            CustomerResponse result = customerService.updateCustomerStatus(id, statusRequest);

            // then
            assertThat(result).isNotNull();
            assertThat(customer.getCustomerStatus()).isEqualTo(CustomerStatus.ACTIVE);
            assertThat(customer.getStatusReason()).isEqualTo("All documents verified");
        }

        @Test
        @DisplayName("should throw CustomerNotFoundException when customer not found for status update")
        void shouldThrowWhenCustomerNotFoundForStatusUpdate() {
            UUID id = UUID.randomUUID();
            CustomerStatusUpdateRequest req = CustomerStatusUpdateRequest.builder()
                    .status(CustomerStatus.SUSPENDED)
                    .reason("Suspicious activity")
                    .build();

            given(customerRepository.findByIdWithAllDetails(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> customerService.updateCustomerStatus(id, req))
                    .isInstanceOf(CustomerNotFoundException.class);
        }
    }

    // ── getOnboardingStatus ───────────────────────────────────────────────────

    @Nested
    @DisplayName("getOnboardingStatus()")
    class GetOnboardingStatus {

        @Test
        @DisplayName("should return onboarding status summary")
        void shouldReturnStatusSummary() {
            // given
            UUID     id       = UUID.randomUUID();
            Customer customer = TestFixtures.buildCustomer();
            OnboardingStatusResponse statusResponse = OnboardingStatusResponse.builder()
                    .customerId(id)
                    .customerStatus(CustomerStatus.ACTIVE)
                    .eligibleForServices(true)
                    .build();

            given(customerRepository.findByIdWithAllDetails(id)).willReturn(Optional.of(customer));
            given(customerMapper.toOnboardingStatusResponse(customer)).willReturn(statusResponse);

            // when
            OnboardingStatusResponse result = customerService.getOnboardingStatus(id);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getEligibleForServices()).isTrue();
        }
    }
}

