package com.example.wide.enumeration;

import lombok.Getter;

@Getter
public enum UserRole {
    CUSTOMER("CUSTOMER"), ADMIN("ADMIN");

    private final String code;

    UserRole(String code) {
        this.code = code;
    }
}
