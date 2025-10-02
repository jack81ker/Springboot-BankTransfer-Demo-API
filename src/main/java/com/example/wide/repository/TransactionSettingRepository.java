package com.example.wide.repository;

import com.example.wide.entities.TransactionSetting;
import com.example.wide.enumeration.Currency;
import com.example.wide.enumeration.PeriodicRestriction;
import com.example.wide.enumeration.TransactionType;
import org.hibernate.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionSettingRepository extends JpaRepository<TransactionSetting, Long> {
    Optional<TransactionSetting> findByTransactionTypeAndCurrencyAndPeriodicRestriction(TransactionType transactionType, Currency currency, PeriodicRestriction periodicRestriction);
}
