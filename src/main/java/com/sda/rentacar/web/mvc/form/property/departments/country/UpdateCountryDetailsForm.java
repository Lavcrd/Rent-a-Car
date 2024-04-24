package com.sda.rentacar.web.mvc.form.property.departments.country;

import com.sda.rentacar.model.property.department.Country;
import com.sda.rentacar.web.mvc.form.common.ConfirmationForm;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@NoArgsConstructor
public class UpdateCountryDetailsForm extends ConfirmationForm {
    @NotBlank(message = "Failure: Name field cannot be blank.")
    private String name;

    @NotBlank(message = "Failure: ISO field cannot be blank.")
    @Pattern(regexp = "(^\\w{2}$)|(^\\w{2}-\\w{2}$)", message = "Failure: Invalid ISO pattern.")
    private String code;

    @Pattern(regexp = "^[+]\\d{1,3}$", message = "Failure: Invalid contact pattern.")
    private String contact;

    public UpdateCountryDetailsForm(Country country) {
        this.name = country.getName();
        this.code = country.getCode();
        this.contact = country.getContact();
    }
}
