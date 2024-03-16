package com.sda.carrental.web.mvc.form.property.departments.country;

import com.sda.carrental.model.property.department.Country;
import com.sda.carrental.web.mvc.form.common.ConfirmationForm;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class UpdateCountryCurrencyForm extends ConfirmationForm {
    @NotNull(message = "Failure: Currency field cannot be blank.")
    private Long currency;

    public UpdateCountryCurrencyForm(Country country) {
        this.currency = country.getCurrency().getId();
    }
}
