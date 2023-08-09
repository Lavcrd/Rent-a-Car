package com.sda.carrental.global;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@NoArgsConstructor
public class ConstantValues {
    // Cost value of returning to different department than lending one
    private double deptReturnPriceDiff = 120;

    // Timeframe in days before the lending date when customers will have to pay a fee if the reservation is cancelled
    private long refundSubtractDaysDuration = 4;

    // Decimal percentage subtracted from main payment value of the cancelled reservation
    private double cancellationFeePercentage = 0.2;

    // Duration of the gap in days required between the return of a reserved car and making another reservation
    private int reservationGap = 3;
}
