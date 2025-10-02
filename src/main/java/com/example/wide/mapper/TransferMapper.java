package com.example.wide.mapper;

import com.example.wide.dto.TransferResponseDto;
import com.example.wide.entities.Transfer;
import org.springframework.stereotype.Component;

@Component
public class TransferMapper {
    public static TransferResponseDto toDto(Transfer transfer) {
        return TransferResponseDto.builder()
                .id(transfer.getId())
                .amount(transfer.getAmount())
                .toAccountNumber(transfer.getToAccount())
                .fromAccountNumber(transfer.getFromAccount())
                .currency(transfer.getCurrency())
                .transactionTime(transfer.getCreatedTimestamp())
                .build();
    }
}
