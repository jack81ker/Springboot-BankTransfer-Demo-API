package com.example.wide.entities;

import com.example.wide.enumeration.Currency;
import com.example.wide.enumeration.PeriodicRestriction;
import com.example.wide.enumeration.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="transaction_setting")
public class TransactionSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PeriodicRestriction periodicRestriction = PeriodicRestriction.PER_DAY;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal maxThreshold;
}
