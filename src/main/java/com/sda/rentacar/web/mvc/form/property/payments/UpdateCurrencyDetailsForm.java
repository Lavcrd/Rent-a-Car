package com.sda.rentacar.web.mvc.form.property.payments;

import com.sda.rentacar.model.property.payments.Currency;
import com.sda.rentacar.web.mvc.form.common.ConfirmationForm;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class UpdateCurrencyDetailsForm extends ConfirmationForm {
    @NotBlank(message="Failure: Field 'name' cannot be empty.")
    @Length(min=2, max=40, message ="Failure: Invalid currency name length.")
    private String name;

    @NotBlank(message="Failure: Field 'code' cannot be empty.")
    @Length(min=3, max=3, message = "Failure: Invalid currency code length.")
    private String code;

    public UpdateCurrencyDetailsForm(Currency currency) {
        this.name = currency.getName();
        this.code = currency.getCode();
    }
}
