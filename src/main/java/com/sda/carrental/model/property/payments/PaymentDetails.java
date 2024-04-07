package com.sda.carrental.model.property.payments;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "payment_details")
@Getter
@NoArgsConstructor
public class PaymentDetails {
    public PaymentDetails(Double carFee, Double divergenceFee, Double requiredDeposit, Double payment, Double deposit, Long reservationId, Currency currency) {
        this.initialCarFee = carFee;
        this.initialDivergenceFee = divergenceFee;
        this.initialDeposit = requiredDeposit;
        this.paymentBalance = payment;
        this.paymentAccepted = 0.0;
        this.deposit = deposit;
        this.secured = 0.0;
        this.releasedDeposit = 0.0;
        this.currency = currency;
        this.reservationId= reservationId;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Setter
    @Column(name = "initial_car_fee", nullable = false)
    private double initialCarFee;

    @Column(name = "initial_divergence_fee", nullable = false)
    private double initialDivergenceFee;

    @Setter
    @Column(name = "initial_deposit", nullable = false)
    private double initialDeposit;

    @Setter
    @Column(name = "payment_balance", nullable = false)
    private double paymentBalance;

    @Setter
    @Column(name = "payment_accepted", nullable = false)
    private double paymentAccepted;

    @Setter
    @Column(name = "deposit", nullable = false)
    private double deposit;

    @Setter
    @Column(name = "released_deposit", nullable = false)
    private double releasedDeposit;

    @Setter
    @Column(name = "secured", nullable = false)
    private double secured;

    @JoinColumn(name = "reservation_id", referencedColumnName = "id")
    private Long reservationId;

    @ManyToOne
    @JoinColumn(name = "currency", referencedColumnName = "id")
    private Currency currency;
}
