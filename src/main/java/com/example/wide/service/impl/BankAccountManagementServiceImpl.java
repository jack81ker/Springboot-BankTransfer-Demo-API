package com.example.wide.service.impl;

import com.example.wide.dto.BankAccountDto;
import com.example.wide.entities.BankAccount;
import com.example.wide.enumeration.Currency;
import com.example.wide.exception.BusinessException;
import com.example.wide.mapper.BankAccountMapper;
import com.example.wide.repository.LedgerRepository;
import com.example.wide.repository.BankAccountRepository;
import com.example.wide.service.BankAccountManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BankAccountManagementServiceImpl implements BankAccountManagementService {
    private final BankAccountRepository bankAccountRepository;
    private final LedgerRepository ledgerRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BankAccountDto> listUserBankAccounts(String authenticatedUsername, Long userId, Pageable pageable) throws BusinessException {
        Page<BankAccount> accounts = bankAccountRepository.findAccountsByOwnerId(userId, pageable);
        if (accounts == null || accounts.isEmpty()) return List.of();

        List<BankAccountDto> bankAccounts = new ArrayList<>();
        for(BankAccount bankAccount: accounts) {
            BankAccountDto bankAccountDto = BankAccountMapper.toDto(bankAccount);
            bankAccountDto.setBalance(ledgerRepository.getBalance(bankAccount));
            bankAccounts.add(bankAccountDto);
        }
        return bankAccounts;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BankAccount> getUserBankAccount(String accountNumber, Currency currency) {
        return bankAccountRepository.findByAccountNumberForUpdate(accountNumber, currency);
    }

    @Override
    public Optional<BankAccount> getUserBankAccount(String accountNumber) {
        return bankAccountRepository.findByAccountNumber(accountNumber);
    }
}
