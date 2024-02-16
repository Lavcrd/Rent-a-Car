package com.sda.carrental.web.mvc.form.property.departments;

import com.sda.carrental.web.mvc.form.validation.constraint.ValidCountry;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class SearchDepartmentsForm {
    @ValidCountry
    private String country;

    private String postcode;

    private String city;

    private String street;

    private String building;

    @NotNull(message = "Failure: Active-Inactive field is invalid")
    private boolean isActive;

    @NotNull(message = "Failure: HQ field is invalid")
    private boolean isHeadquarter;

    public SearchDepartmentsForm() {
        this.isHeadquarter = false;
        this.isActive = true;
    }
}
