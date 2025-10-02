package com.example.wide.repository;

import com.example.wide.entities.Ledger;
import com.example.wide.entities.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface LedgerRepository extends JpaRepository<Ledger, Long> {
    @Query("SELECT COALESCE(SUM(CASE WHEN l.type='CREDIT' THEN l.amount ELSE -l.amount END), 0) " +
            "FROM Ledger l WHERE l.account = :account")
    BigDecimal getBalance(@Param("account") BankAccount account);

    @Query("SELECT COALESCE(SUM(l.amount), 0) " +
            "FROM Ledger l WHERE l.account = :account " +
            "AND l.type = 'DEBIT' " +
            "AND l.timestamp >= :today")
    BigDecimal getDailyDebits(@Param("account") BankAccount account, @Param("today") LocalDateTime today);
}
