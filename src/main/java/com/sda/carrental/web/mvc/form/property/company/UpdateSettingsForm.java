package com.sda.carrental.web.mvc.form.property.company;

import com.sda.carrental.model.property.company.Settings;
import com.sda.carrental.web.mvc.form.common.ConfirmationForm;
import com.sda.carrental.web.mvc.form.validation.constraint.NumericString;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class UpdateSettingsForm extends ConfirmationForm {
    @NumericString(message = "Failure: Cancellation timeframe value is not valid.", allowedDouble = false, allowedNegative = false)
    @NotNull(message = "Failure: Cancellation timeframe field cannot be empty.")
    private String refundSubtractDaysDuration;

    @NotNull(message = "Failure: Deadline field cannot be empty.")
    @NumericString(message = "Failure: Refund deadline must be positive number value.", allowedDouble = false, allowedNegative = false, allowedZero = false)
    private String refundDepositDeadlineDays;

    @NumericString(message = "Failure: Cancellation fee incorrect format.", allowedNegative = false, max = 1D)
    @NotBlank(message = "Failure: Cancellation fee field cannot be empty.")
    private String cancellationFeePercentage;

    @NumericString(message = "Failure: Car cooldown cannot be negative.", allowedDouble = false, allowedNegative = false)
    @NotNull(message = "Failure: Car cooldown field cannot be empty.")
    private String reservationGap;

    public UpdateSettingsForm(Settings settings) {
        this.refundSubtractDaysDuration = String.valueOf(settings.getRefundSubtractDaysDuration());
        this.refundDepositDeadlineDays = String.valueOf(settings.getRefundDepositDeadlineDays());
        this.cancellationFeePercentage = String.valueOf(settings.getCancellationFeePercentage());
        this.reservationGap = String.valueOf(settings.getReservationGap());
    }
}
