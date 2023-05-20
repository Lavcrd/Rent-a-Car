package com.sda.carrental.model.property;

import com.sda.carrental.model.operational.Reservation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "payment_details")
@Getter
@NoArgsConstructor
public class PaymentDetails {
    public PaymentDetails(Double mainValue, Double deposit, Reservation reservation) {
        this.mainValue = mainValue;
        this.deposit = deposit;
        this.securedValue = 0.0;
        this.reservation = reservation;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @OneToOne
    @JoinColumn(name = "reservation_id", referencedColumnName = "reservation_id")
    private Reservation reservation;

    @Setter
    @Column(name = "main_value", nullable = false)
    private double mainValue;

    @Setter
    @Column(name = "deposit", nullable = false)
    private double deposit;

    @Setter
    @Column(name = "secured_value", nullable = false)
    private double securedValue;
}