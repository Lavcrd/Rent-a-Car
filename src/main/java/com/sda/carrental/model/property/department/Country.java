package com.sda.carrental.model.property.department;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "country")
@Getter
@Setter
@NoArgsConstructor
public class Country {
    public Country(String name, String code, String contact, String currency, Double exchange) {
        this.name = name;
        this.code = code;
        this.contact = contact;
        this.currency = currency;
        this.exchange = exchange;
        this.isActive = false;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "contact", nullable = false)
    private String contact;

    @Column(name = "currency_used", nullable = false)
    private String currency;

    @Column(name = "exchange_rate", nullable = false)
    private Double exchange;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;
}
