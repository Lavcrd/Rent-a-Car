package com.sda.rentacar.web.mvc.form.property.payments;

import com.sda.rentacar.web.mvc.form.common.ConfirmationForm;
import com.sda.rentacar.web.mvc.form.validation.constraint.NumericString;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
public class DepositForm extends ConfirmationForm {
    @NotEmpty(message = "Failure: Field must contain a value.")
    @NumericString(allowedNegative = false, allowedZero = false)
    private String value;
}
