package com.sda.rentacar.web.mvc.form.property.departments.country;

import com.sda.rentacar.model.property.department.Country;
import com.sda.rentacar.web.mvc.form.common.ConfirmationForm;
import com.sda.rentacar.web.mvc.form.validation.constraint.NumericString;
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
