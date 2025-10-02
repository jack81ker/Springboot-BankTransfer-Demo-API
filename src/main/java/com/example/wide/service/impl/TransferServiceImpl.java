package com.example.wide.service.impl;

import com.example.wide.dto.TransferResponseDto;
import com.example.wide.entities.*;
import com.example.wide.enumeration.*;
import com.example.wide.exception.BusinessException;
import com.example.wide.mapper.TransferMapper;
import com.example.wide.repository.LedgerRepository;
import com.example.wide.repository.TransactionSettingRepository;
import com.example.wide.repository.TransferRepository;
import com.example.wide.service.BankAccountManagementService;
import com.example.wide.service.TransferLogService;
import com.example.wide.service.TransferService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {
    private final Logger logger = LogManager.getLogger(TransferServiceImpl.class);

    private final BankAccountManagementService bankAccountManagementService;
    private final TransferLogService transferLogService;

    private final LedgerRepository ledgerRepository;
    private final TransferRepository transferRepository;
    private final TransactionSettingRepository transactionSettingRepository;

    @Override
    @Transactional
    public TransferResponseDto transfer(String authenticatedUserName, String fromAccountNumber,
                                        String toAccountNumber, Currency currency,
                                        BigDecimal transferAmount) throws BusinessException {
        logger.info("Transfering from {} to {} - amount: {} {}", fromAccountNumber, toAccountNumber, currency, transferAmount);

        TransferLog.TransferLogBuilder logBuilder = buildInitialLog(fromAccountNumber, toAccountNumber, transferAmount, currency);
        transferLogService.saveTransferLog(logBuilder.build());

        validateNotSameAccount(fromAccountNumber, toAccountNumber, logBuilder);

        BankAccount fromAccount = getAndValidateFromAccount(authenticatedUserName, fromAccountNumber, currency, logBuilder);
        BankAccount toAccount = getAndValidateToAccount(toAccountNumber, currency, logBuilder);

        Transfer transfer = createPendingTransfer(fromAccountNumber, toAccountNumber, currency, transferAmount);
        createTransferLog(transfer, TransferStatus.PENDING, null);

        validateBalance(fromAccount, transfer, transferAmount, logBuilder);
        validateDailyLimit(fromAccount, currency, transferAmount, transfer, logBuilder);

        processLedgerEntries(transfer, fromAccount, toAccount);

        markTransferSuccess(transfer, logBuilder);
        logger.info("Success transfer {} {} from {} to {} ", currency, transferAmount, fromAccountNumber, toAccountNumber);

        return TransferMapper.toDto(transfer);
    }

    @Override
    public List<TransferResponseDto> listTransferHistory(String authenticatedUserName, boolean isAdmin, String accountNumber, Pageable pageable) throws BusinessException{
        BankAccount bankAccount = bankAccountManagementService.getUserBankAccount(accountNumber).orElseThrow(() -> new BusinessException("Account number is invalid."));
        if(!bankAccount.getOwner().getUsername().equals(authenticatedUserName) && !isAdmin) {
            throw new BusinessException("Forbidden access.");
        }

        Page<Transfer> userTransfers = transferRepository.findTransfersByAccountNumber(accountNumber, pageable);
        if(userTransfers==null || userTransfers.isEmpty()) return List.of();

        return userTransfers.stream().map(TransferMapper::toDto).toList();
    }

    private TransferLog.TransferLogBuilder buildInitialLog(String fromAccount, String toAccount,
                                                           BigDecimal amount, Currency currency) {
        return TransferLog.builder()
                .toAccount(toAccount)
                .fromAccount(fromAccount)
                .amount(amount)
                .currency(currency)
                .logTime(System.currentTimeMillis())
                .status(TransferStatus.PENDING);
    }

    private void validateNotSameAccount(String fromAccount, String toAccount, TransferLog.TransferLogBuilder logBuilder) {
        if (fromAccount.equals(toAccount)) {
            failAndThrow(logBuilder, "Transferring to the same account is forbidden.");
        }
    }

    private BankAccount getAndValidateFromAccount(String authenticatedUserName, String fromAccountNumber,
                                                  Currency currency, TransferLog.TransferLogBuilder logBuilder) {
        BankAccount account = bankAccountManagementService.getUserBankAccount(fromAccountNumber, currency).orElseThrow(
                () -> failAndReturn(logBuilder, "From bank account is not found.")
        );

        if (!account.getOwner().getUsername().equals(authenticatedUserName)) {
            failAndThrow(logBuilder, "Forbidden to transfer third party account fund.");
        }
        return account;
    }

    private BankAccount getAndValidateToAccount(String toAccountNumber, Currency currency,
                                                TransferLog.TransferLogBuilder logBuilder) {
        return bankAccountManagementService.getUserBankAccount(toAccountNumber, currency).orElseThrow(
                () -> failAndReturn(logBuilder, "To bank account is not found.")
        );
    }

    private Transfer createPendingTransfer(String fromAccount, String toAccount,
                                           Currency currency, BigDecimal amount) {
        return transferRepository.save(
                Transfer.builder()
                        .fromAccount(fromAccount)
                        .toAccount(toAccount)
                        .status(TransferStatus.PENDING)
                        .createdTimestamp(System.currentTimeMillis())
                        .amount(amount)
                        .currency(currency)
                        .build()
        );
    }

    private void validateBalance(BankAccount fromAccount, Transfer transfer, BigDecimal transferAmount,
                                 TransferLog.TransferLogBuilder logBuilder) {
        BigDecimal balance = ledgerRepository.getBalance(fromAccount);
        if (balance.compareTo(transferAmount) < 0) {
            failAndThrow(logBuilder.transferId(transfer.getId()), "Source bank account insufficient balance.");
        }
    }

    private void validateDailyLimit(BankAccount fromAccount, Currency currency, BigDecimal transferAmount,
                                    Transfer transfer, TransferLog.TransferLogBuilder logBuilder) {
        TransactionSetting setting = transactionSettingRepository
                .findByTransactionTypeAndCurrencyAndPeriodicRestriction(TransactionType.TRANSFER, currency, PeriodicRestriction.PER_DAY)
                .orElseThrow();

        BigDecimal todayDebits = ledgerRepository.getDailyDebits(fromAccount, LocalDate.now().atStartOfDay());

        if (todayDebits.add(transferAmount).compareTo(setting.getMaxThreshold()) > 0) {
            failAndThrow(logBuilder.transferId(transfer.getId()), "Source bank account hit daily limit threshold.");
        }
    }

    private void processLedgerEntries(Transfer transfer, BankAccount fromAccount, BankAccount toAccount) {
        ledgerRepository.save(Ledger.builder()
                .account(fromAccount)
                .amount(transfer.getAmount())
                .timestamp(LocalDateTime.now())
                .type(LedgerType.DEBIT)
                .reference("Transfer:" + transfer.getId())
                .build());

        ledgerRepository.save(Ledger.builder()
                .account(toAccount)
                .amount(transfer.getAmount())
                .timestamp(LocalDateTime.now())
                .type(LedgerType.CREDIT)
                .reference("Transfer:" + transfer.getId())
                .build());
    }

    private void markTransferSuccess(Transfer transfer, TransferLog.TransferLogBuilder logBuilder) {
        transfer.setStatus(TransferStatus.SUCCESS);
        transfer.setUpdatedTimestamp(System.currentTimeMillis());
        transferRepository.save(transfer);

        logBuilder.transferId(transfer.getId())
                .status(TransferStatus.SUCCESS)
                .logTime(System.currentTimeMillis())
                .errorMessage(null);

        transferLogService.saveTransferLog(logBuilder.build());
    }

    private void failAndThrow(TransferLog.TransferLogBuilder logBuilder, String message) {
        transferLogService.saveTransferLog(logBuilder
                .status(TransferStatus.FAILED)
                .logTime(System.currentTimeMillis())
                .errorMessage(message)
                .build());
        throw new BusinessException(message);
    }

    private BusinessException failAndReturn(TransferLog.TransferLogBuilder logBuilder, String message) {
        transferLogService.saveTransferLog(logBuilder
                .status(TransferStatus.FAILED)
                .logTime(System.currentTimeMillis())
                .errorMessage(message)
                .build());
        return new BusinessException(message);
    }

    private void createTransferLog(Transfer transfer, TransferStatus status, String errorMessage) {
        transferLogService.saveTransferLog(TransferLog.builder()
                .transferId(transfer.getId())
                .amount(transfer.getAmount())
                .fromAccount(transfer.getFromAccount())
                .toAccount(transfer.getToAccount())
                .currency(transfer.getCurrency())
                .status(status)
                .logTime(System.currentTimeMillis())
                .errorMessage(errorMessage)
                .build());
    }
}
