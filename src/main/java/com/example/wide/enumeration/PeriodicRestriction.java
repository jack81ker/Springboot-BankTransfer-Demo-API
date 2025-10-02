package com.example.wide.enumeration;

import lombok.Getter;

@Getter
public enum PeriodicRestriction {
    PER_DAY("DAY");

    private final String code;

    PeriodicRestriction(String code) {
        this.code = code;
    }
}
