package com.sda.carrental.web.mvc.form.property.company;

import com.sda.carrental.model.property.company.Settings;
import com.sda.carrental.web.mvc.form.common.ConfirmationForm;
import com.sda.carrental.web.mvc.form.validation.constraint.NumberValue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class UpdateSettingsForm extends ConfirmationForm {
    @NumberValue(message = "Failure: Cancellation timeframe value is not valid.", allowedZero = true, allowedPositive = true)
    @NotNull(message = "Failure: Cancellation timeframe field cannot be empty.")
    private String refundSubtractDaysDuration;

    @NotNull(message = "Failure: Deadline field cannot be empty.")
    @NumberValue(message = "Failure: Refund deadline must be positive number value.", allowedPositive = true)
    private String refundDepositDeadlineDays;

    @NumberValue(message = "Failure: Cancellation fee incorrect format.", allowedDouble = true, allowedZero = true, allowedPositive = true,  max = 1D)
    @NotBlank(message = "Failure: Cancellation fee field cannot be empty.")
    private String cancellationFeePercentage;

    @NumberValue(message = "Failure: Car cooldown cannot be negative.", allowedZero = true, allowedPositive = true)
    @NotNull(message = "Failure: Car cooldown field cannot be empty.")
    private String reservationGap;

    public UpdateSettingsForm(Settings settings) {
        this.refundSubtractDaysDuration = String.valueOf(settings.getRefundSubtractDaysDuration());
        this.refundDepositDeadlineDays = String.valueOf(settings.getRefundDepositDeadlineDays());
        this.cancellationFeePercentage = String.valueOf(settings.getCancellationFeePercentage());
        this.reservationGap = String.valueOf(settings.getReservationGap());
    }
}
