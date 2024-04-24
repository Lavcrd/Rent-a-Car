package com.sda.rentacar.model.property.company;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "settings")
@Getter
@Setter
@NoArgsConstructor
public class Settings {
    public Settings(long refundSubtractDaysDuration, long refundDepositDeadlineDays, double cancellationFeePercentage, int reservationGap) {
        this.refundSubtractDaysDuration = refundSubtractDaysDuration;
        this.refundDepositDeadlineDays = refundDepositDeadlineDays;
        this.cancellationFeePercentage = cancellationFeePercentage;
        this.reservationGap = reservationGap;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // Timeframe in days before the lending date when customers will have to pay a fee if the reservation is cancelled
    @Column(name = "refund_penalty_timeframe", nullable = false)
    private long refundSubtractDaysDuration = 4;

    // Timeframe in working days before deposit must be refunded to customer
    @Column(name = "refund_management_timeframe", nullable = false)
    private long refundDepositDeadlineDays = 5;

    // Decimal percentage subtracted from main payment value of the cancelled reservation
    @Column(name = "cancellation_fee_percentage", nullable = false)
    private double cancellationFeePercentage = 0.2;

    // Duration of the gap in days required between the return of a reserved car and making another reservation
    @Column(name = "reservation_gap", nullable = false)
    private int reservationGap = 4;
}
