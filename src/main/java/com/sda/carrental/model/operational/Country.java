package com.sda.carrental.model.operational;

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
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "contact", unique = true)
    private String contact;

    @Column(name = "currency_used")
    private String currency;

    @Column(name = "exchange_rate")
    private Double exchange;
}
