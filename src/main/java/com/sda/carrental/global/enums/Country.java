package com.sda.carrental.global.enums;

import lombok.Getter;

@Getter
public enum Country {
    COUNTRY_PL("Poland", "PL", "+48"), COUNTRY_GB("Great Britain", "GB", "+44"), COUNTRY_NL("Netherlands", "NL", "+31"), COUNTRY_DE("Germany", "DE", "+49");

    final String countryName;
    final String code;
    final String contactCode;
    Country(String countryName, String code, String contactCode) {
        this.countryName = countryName;
        this.code = code;
        this.contactCode = contactCode;
    }
}
