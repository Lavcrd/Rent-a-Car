package com.sda.rentacar.web.mvc.form.property.departments.country;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class SearchCountriesForm {
    private String name;

    private String code;

    private String currency;

    @NotNull(message = "Failure: Active-Inactive field is invalid")
    private boolean isActive;

    public SearchCountriesForm() {
        this.isActive = true;
    }
}
