package com.sda.carrental.web.mvc.form.property.payments;

import com.sda.carrental.web.mvc.form.common.ConfirmationForm;
import com.sda.carrental.web.mvc.form.validation.constraint.DoubleValue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
public class DepositForm extends ConfirmationForm {
    @NotEmpty(message = "Failure: Field must contain a value.")
    @DoubleValue
    private String value;
}
