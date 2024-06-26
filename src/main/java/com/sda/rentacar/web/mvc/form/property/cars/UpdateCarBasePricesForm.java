package com.sda.rentacar.web.mvc.form.property.cars;

import com.sda.rentacar.web.mvc.form.common.ConfirmationForm;
import com.sda.rentacar.web.mvc.form.validation.constraint.NumericString;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Positive;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class UpdateCarBasePricesForm extends ConfirmationForm {
    @Positive(message = "Failure: Price value must be positive.")
    @NumericString(allowedNegative = false, allowedZero = false)
    private String price;

    @Positive(message = "Failure: Deposit value must be positive.")
    @NumericString(allowedNegative = false, allowedZero = false)
    private String deposit;
}
