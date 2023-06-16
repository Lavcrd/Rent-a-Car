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
    public PaymentDetails(Double reqRaw, Double reqReturn, Double reqDeposit, Double mainValue, Double deposit, Reservation reservation) {
        this.requiredRawValue = reqRaw;
        this.requiredReturnValue = reqReturn;
        this.requiredDeposit = reqDeposit;
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

    @Column(name = "archival_raw_value", nullable = false)
    private double requiredRawValue;

    @Column(name = "archival_return_value", nullable = false)
    private double requiredReturnValue;

    @Setter
    @Column(name = "archival_deposit", nullable = false)
    private double requiredDeposit;

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
