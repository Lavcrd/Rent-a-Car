package com.sda.rentacar.model.property.department;

import com.sda.rentacar.model.property.payments.Currency;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "country")
@Getter
@Setter
@NoArgsConstructor
public class Country {
    public Country(String name, String code, String contact, Currency currency) {
        this.name = name;
        this.code = code;
        this.contact = contact;
        this.currency = currency;
        this.isActive = false;
        this.relocateCarPrice = 30.0;
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

    @ManyToOne
    @JoinColumn(name = "currency", referencedColumnName = "id")
    private Currency currency;

    @Column(name = "relocate_car_eur", nullable = false)
    private Double relocateCarPrice;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;
}
