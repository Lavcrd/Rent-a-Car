package com.sda.rentacar.model.property.payments;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity(name = "currency")
@Getter
@NoArgsConstructor
public class Currency {
    public Currency(String name, String code, Double exchange) {
        this.name = name;
        this.code = code;
        this.exchange = exchange;
        this.updated = LocalDate.now();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Setter
    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Setter
    @Column(name = "exchange_rate", nullable = false)
    private Double exchange;

    @Setter
    @Column(name = "updated", nullable = false)
    private LocalDate updated;
}
