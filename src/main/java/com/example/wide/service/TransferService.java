package com.example.wide.service;

import com.example.wide.dto.TransferResponseDto;
import com.example.wide.enumeration.Currency;
import com.example.wide.exception.BusinessException;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface TransferService {

    TransferResponseDto transfer(String authenticatedUserName, String sourceAccountNumber, String destinationAccountNumber, Currency currency, BigDecimal transferAmount) throws BusinessException;

    List<TransferResponseDto> listTransferHistory(String authenticatedUserName, boolean isAdmin, String accountNumber, Pageable pageable);
}
