package com.sda.rentacar.web.mvc.form.property.payments;

import com.sda.rentacar.web.mvc.form.common.ConfirmationForm;
import com.sda.rentacar.web.mvc.form.validation.constraint.NumericString;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class PaymentForm extends ConfirmationForm {
    @NotNull(message = "Failure: Missing operation value.")
    private Long reservationId;

    @NumericString(message = "Failure: Incorrect deposit value.")
    private String deposit;

    @NumericString(message = "Failure: Incorrect payment value.")
    private String payment;

    @NotNull(message = "Failure: Missing payment type.")
    private boolean isCard;

    public PaymentForm(Long reservationId) {
        this.reservationId = reservationId;
        this.deposit = String.valueOf(0.0);
        this.payment = String.valueOf(0.0);
        this.isCard = false;
    }
}
