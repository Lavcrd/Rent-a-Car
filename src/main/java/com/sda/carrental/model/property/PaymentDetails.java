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
    public PaymentDetails(Double carFee, Double divergenceFee, Double requiredDeposit, Double payment, Double deposit, Reservation reservation) {
        this.initialCarFee = carFee;
        this.initialDivergenceFee = divergenceFee;
        this.initialDeposit = requiredDeposit;
        this.payment = payment;
        this.deposit = deposit;
        this.secured = 0.0;
        this.releasedDeposit = 0.0;
        this.reservation = reservation;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne
    @JoinColumn(name = "reservation", referencedColumnName = "id")
    private Reservation reservation;

    @Column(name = "initial_car_fee", nullable = false)
    private double initialCarFee;

    @Column(name = "initial_divergence_fee", nullable = false)
    private double initialDivergenceFee;

    @Setter
    @Column(name = "initial_deposit", nullable = false)
    private double initialDeposit;

    @Setter
    @Column(name = "payment_balance", nullable = false)
    private double payment;

    @Setter
    @Column(name = "deposit", nullable = false)
    private double deposit;

    @Setter
    @Column(name = "released_deposit", nullable = false)
    private double releasedDeposit;

    @Setter
    @Column(name = "secured", nullable = false)
    private double secured;
}
