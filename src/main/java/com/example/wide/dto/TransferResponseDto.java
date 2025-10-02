package com.example.wide.dto;

import com.example.wide.enumeration.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponseDto {
    private Long id;
    private String fromAccountNumber;
    private String toAccountNumber;
    private Currency currency;
    private BigDecimal amount;
    private Long transactionTime;
}
