package com.sda.carrental.web.mvc.form.operational;

import com.sda.carrental.web.mvc.form.common.ConfirmationForm;
import com.sda.carrental.web.mvc.form.validation.constraint.DoubleValue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class PaymentForm extends ConfirmationForm {
    @NotNull(message = "Failure: Missing operation value")
    private Long reservationId;

    @DoubleValue(message = "Failure: Incorrect deposit value", allowedPositive = true, allowedZero = true, allowedNegative = true)
    private String deposit;

    @DoubleValue(message = "Failure: Incorrect payment value", allowedPositive = true, allowedZero = true, allowedNegative = true)
    private String payment;

    public PaymentForm(Long reservationId) {
        this.reservationId = reservationId;
        this.deposit = String.valueOf(0.0);
        this.payment = String.valueOf(0.0);
    }
}
