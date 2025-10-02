package com.example.wide.service;

import com.example.wide.dto.BankAccountDto;
import com.example.wide.entities.BankAccount;
import com.example.wide.enumeration.Currency;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface BankAccountManagementService {
    List<BankAccountDto> listUserBankAccounts(String authenticatedUserName, Long userId, Pageable pageable);

    Optional<BankAccount> getUserBankAccount(String accountNumber, Currency currency) ;

    Optional<BankAccount> getUserBankAccount(String accountNumber);
}
