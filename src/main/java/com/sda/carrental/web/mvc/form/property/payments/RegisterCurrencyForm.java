package com.sda.carrental.web.mvc.form.property.payments;

import com.sda.carrental.web.mvc.form.common.ConfirmationForm;
import com.sda.carrental.web.mvc.form.validation.constraint.NumericString;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class RegisterCurrencyForm extends ConfirmationForm {
    @NotBlank(message="Failure: Field 'name' cannot be empty.")
    @Length(min=2, max=40, message ="Failure: Invalid currency name length.")
    private String name;

    @NotBlank(message="Failure: Field 'code' cannot be empty.")
    @Length(min=3, max=3, message = "Failure: Invalid currency code length.")
    private String code;

    @NotBlank(message="Failure: Field 'value' cannot be empty.")
    @NumericString(allowedNegative = false)
    private String exchange;
}
