package com.example.wide.repository;

import com.example.wide.entities.BankAccount;
import com.example.wide.enumeration.Currency;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    @Query(value = """
        SELECT ua.* FROM bank_accounts ua WHERE owner_id = :ownerId
        """,
    countQuery = """
        SELECT count(1) FROM bank_accounts ua WHERE owner_id = :ownerId
        """, nativeQuery = true)
    Page<BankAccount> findAccountsByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    Optional<BankAccount> findByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BankAccount b WHERE b.accountNumber = :accountNumber AND b.currency = :currency")
    Optional<BankAccount> findByAccountNumberForUpdate(@Param("accountNumber") String accountNumber, @Param("currency") Currency currency);
}
