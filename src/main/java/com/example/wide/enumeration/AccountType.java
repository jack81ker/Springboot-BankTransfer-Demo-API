package com.example.wide.enumeration;

import lombok.Getter;

@Getter
public enum AccountType {
    SAVINGS("S"), CURRENT("C");

    private final String code;

    AccountType(String code) {
        this.code = code;
    }
}
