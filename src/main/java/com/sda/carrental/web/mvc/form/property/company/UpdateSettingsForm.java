package com.sda.carrental.web.mvc.form.property.company;

import com.sda.carrental.web.mvc.form.validation.constraint.DoubleValue;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Getter
@Setter
public class UpdateSettingsForm {
    @PositiveOrZero
    private long refundSubtractDaysDuration;

    @Positive
    private long refundDepositDeadlineDays;

    @DoubleValue(allowedPositive = true)
    private Double cancellationFeePercentage;

    @PositiveOrZero
    private long reservationGap;
}
