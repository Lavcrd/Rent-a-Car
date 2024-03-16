package com.sda.carrental.web.mvc.form.property.departments.country;

import com.sda.carrental.model.property.department.Country;
import com.sda.carrental.web.mvc.form.common.ConfirmationForm;
import com.sda.carrental.web.mvc.form.validation.constraint.NumericString;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateRelocationFeeForm extends ConfirmationForm {
    @NumericString(allowedNegative = false)
    private String fee;

    public UpdateRelocationFeeForm(Country country) {
        this.fee = String.valueOf(country.getRelocateCarPrice());
    }
}
