package com.example.wide.service.impl;

import com.example.wide.dto.TransferResponseDto;
import com.example.wide.entities.*;
import com.example.wide.enumeration.Currency;
import com.example.wide.enumeration.PeriodicRestriction;
import com.example.wide.enumeration.TransactionType;
import com.example.wide.enumeration.TransferStatus;
import com.example.wide.exception.BusinessException;
import com.example.wide.repository.LedgerRepository;
import com.example.wide.repository.TransactionSettingRepository;
import com.example.wide.repository.TransferRepository;
import com.example.wide.service.BankAccountManagementService;
import com.example.wide.service.TransferLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransferServiceImplTest {

    @Mock
    private BankAccountManagementService bankAccountManagementService;
    @Mock
    private TransferLogService transferLogService;
    @Mock
    private LedgerRepository ledgerRepository;
    @Mock
    private TransferRepository transferRepository;
    @Mock
    private TransactionSettingRepository transactionSettingRepository;

    @InjectMocks
    private TransferServiceImpl transferService;

    private BankAccount fromAccount;
    private BankAccount toAccount;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        User owner = User.builder().username("john").build();
        fromAccount = BankAccount.builder().accountNumber("A001").owner(owner).build();
        toAccount = BankAccount.builder().accountNumber("A002").owner(User.builder().username("jane").build()).build();
    }

    @Test
    void testTransfer_Success() {
        BigDecimal amount = BigDecimal.valueOf(100);
        Transfer savedTransfer = Transfer.builder()
                .id(1L)
                .fromAccount("A001")
                .toAccount("A002")
                .amount(amount)
                .currency(Currency.USD)
                .status(TransferStatus.PENDING)
                .build();

        when(bankAccountManagementService.getUserBankAccount("A001", Currency.USD))
                .thenReturn(Optional.of(fromAccount));
        when(bankAccountManagementService.getUserBankAccount("A002", Currency.USD))
                .thenReturn(Optional.of(toAccount));

        when(transferRepository.save(any(Transfer.class))).thenReturn(savedTransfer);
        when(ledgerRepository.getBalance(fromAccount)).thenReturn(BigDecimal.valueOf(1000));
        when(transactionSettingRepository.findByTransactionTypeAndCurrencyAndPeriodicRestriction(
                eq(TransactionType.TRANSFER), eq(Currency.USD), eq(PeriodicRestriction.PER_DAY)))
                .thenReturn(Optional.of(TransactionSetting.builder().maxThreshold(BigDecimal.valueOf(5000)).build()));
        when(ledgerRepository.getDailyDebits(eq(fromAccount), any()))
                .thenReturn(BigDecimal.valueOf(0));

        TransferResponseDto response = transferService.transfer("john", "A001", "A002", Currency.USD, amount);

        assertNotNull(response);
        assertEquals("A001", response.getFromAccountNumber());
        assertEquals("A002", response.getToAccountNumber());
        assertEquals(amount, response.getAmount());
        verify(transferRepository, atLeastOnce()).save(any(Transfer.class));
        verify(ledgerRepository, times(2)).save(any(Ledger.class));
        verify(transferLogService, atLeast(2)).saveTransferLog(any(TransferLog.class));
    }

    @Test
    void testTransfer_Fail_SameAccount() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                transferService.transfer("john", "A001", "A001", Currency.USD, BigDecimal.TEN)
        );
        assertEquals("Transferring to the same account is forbidden.", ex.getMessage());
        verify(transferLogService, atLeastOnce()).saveTransferLog(any());
    }

    @Test
    void testTransfer_Fail_FromAccountNotOwned() {
        fromAccount.setOwner(User.builder().username("other").build());

        when(bankAccountManagementService.getUserBankAccount("A001", Currency.USD))
                .thenReturn(Optional.of(fromAccount));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                transferService.transfer("john", "A001", "A002", Currency.USD, BigDecimal.TEN)
        );
        assertEquals("Forbidden to transfer third party account fund.", ex.getMessage());
    }

    @Test
    void testTransfer_Fail_InsufficientBalance() {
        when(bankAccountManagementService.getUserBankAccount("A001", Currency.USD))
                .thenReturn(Optional.of(fromAccount));
        when(bankAccountManagementService.getUserBankAccount("A002", Currency.USD))
                .thenReturn(Optional.of(toAccount));

        Transfer pending = Transfer.builder().id(10L).build();
        when(transferRepository.save(any(Transfer.class))).thenReturn(pending);
        when(ledgerRepository.getBalance(fromAccount)).thenReturn(BigDecimal.ZERO);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                transferService.transfer("john", "A001", "A002", Currency.USD, BigDecimal.TEN)
        );
        assertEquals("Source bank account insufficient balance.", ex.getMessage());
    }

    @Test
    void testTransfer_Fail_DailyLimitExceeded() {
        when(bankAccountManagementService.getUserBankAccount("A001", Currency.USD))
                .thenReturn(Optional.of(fromAccount));
        when(bankAccountManagementService.getUserBankAccount("A002", Currency.USD))
                .thenReturn(Optional.of(toAccount));

        Transfer pending = Transfer.builder().id(11L).amount(BigDecimal.TEN).currency(Currency.USD).build();
        when(transferRepository.save(any(Transfer.class))).thenReturn(pending);
        when(ledgerRepository.getBalance(fromAccount)).thenReturn(BigDecimal.valueOf(1000));
        when(transactionSettingRepository.findByTransactionTypeAndCurrencyAndPeriodicRestriction(
                eq(TransactionType.TRANSFER), eq(Currency.USD), eq(PeriodicRestriction.PER_DAY)))
                .thenReturn(Optional.of(TransactionSetting.builder().maxThreshold(BigDecimal.valueOf(100)).build()));
        when(ledgerRepository.getDailyDebits(eq(fromAccount), any())).thenReturn(BigDecimal.valueOf(95));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                transferService.transfer("john", "A001", "A002", Currency.USD, BigDecimal.TEN)
        );
        assertEquals("Source bank account hit daily limit threshold.", ex.getMessage());
    }

    @Test
    void testListTransferHistory_Success() {
        when(bankAccountManagementService.getUserBankAccount("A001"))
                .thenReturn(Optional.of(fromAccount));
        Transfer t1 = Transfer.builder().id(1L).fromAccount("A001").toAccount("A002")
                .amount(BigDecimal.TEN).currency(Currency.USD).status(TransferStatus.SUCCESS).build();
        Page<Transfer> page = new PageImpl<>(List.of(t1));
        when(transferRepository.findTransfersByAccountNumber(eq("A001"), any(PageRequest.class)))
                .thenReturn(page);

        List<TransferResponseDto> result =
                transferService.listTransferHistory("john", false, "A001", PageRequest.of(0, 10));

        assertEquals(1, result.size());
        assertEquals("A001", result.get(0).getFromAccountNumber());
    }

    @Test
    void testAdminListTransferHistory_Success() {
        fromAccount.setOwner(User.builder().username("john").build());
        when(bankAccountManagementService.getUserBankAccount("A001"))
                .thenReturn(Optional.of(fromAccount));
        Transfer t1 = Transfer.builder().id(1L).fromAccount("A001").toAccount("A002")
                .amount(BigDecimal.TEN).currency(Currency.USD).status(TransferStatus.SUCCESS).build();
        Page<Transfer> page = new PageImpl<>(List.of(t1));
        when(transferRepository.findTransfersByAccountNumber(eq("A001"), any(PageRequest.class)))
                .thenReturn(page);

        List<TransferResponseDto> result =
                transferService.listTransferHistory("admin", true, "A001", PageRequest.of(0, 10));

        assertEquals(1, result.size());
        assertEquals("A001", result.get(0).getFromAccountNumber());
    }


    @Test
    void testListTransferHistory_Forbidden() {
        fromAccount.setOwner(User.builder().username("other").build());
        when(bankAccountManagementService.getUserBankAccount("A001"))
                .thenReturn(Optional.of(fromAccount));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                transferService.listTransferHistory("john", false, "A001", PageRequest.of(0, 10))
        );
        assertEquals("Forbidden access.", ex.getMessage());
    }
}
