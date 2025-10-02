package com.example.wide.dto;

import com.example.wide.enumeration.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountDto {
    private Long accountId;
    private String accountNumber;
    private String accountType;
    private Currency currency;
    private BigDecimal balance;
}