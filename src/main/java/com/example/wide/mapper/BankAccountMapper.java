package com.example.wide.mapper;

import com.example.wide.dto.BankAccountDto;
import com.example.wide.entities.BankAccount;
import org.springframework.stereotype.Component;

@Component
public class BankAccountMapper {
    public static BankAccountDto toDto(BankAccount bankAccount) {
        return BankAccountDto.builder()
                .accountId(bankAccount.getId())
                .accountNumber(bankAccount.getAccountNumber())
                .accountType(bankAccount.getAccountType().getCode())
                .currency(bankAccount.getCurrency())
                .build();
    }
}
