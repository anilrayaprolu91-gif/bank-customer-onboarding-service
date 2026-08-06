package com.bank.onboarding.mapper;

import com.bank.onboarding.accounts.domain.Account;
import com.bank.onboarding.dto.request.AccountCreationRequest;
import com.bank.onboarding.dto.response.AccountResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AccountMapper {

    @Mapping(target = "id",            ignore = true)
    @Mapping(target = "accountNumber", ignore = true)
    @Mapping(target = "customer",      ignore = true)
    @Mapping(target = "balance",       source = "initialDeposit")
    @Mapping(target = "accountStatus", constant = "PENDING_ACTIVATION")
    @Mapping(target = "openedDate",    expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "closedDate",    ignore = true)
    @Mapping(target = "interestRate",  ignore = true)
    @Mapping(target = "createdAt",     ignore = true)
    @Mapping(target = "updatedAt",     ignore = true)
    Account toAccount(AccountCreationRequest request);

    @Mapping(target = "customerId", source = "customer.id")
    AccountResponse toResponse(Account account);

    List<AccountResponse> toResponseList(List<Account> accounts);
}


