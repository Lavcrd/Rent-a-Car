package com.sda.carrental.web.mvc.form;

import com.sda.carrental.global.enums.Country;
import com.sda.carrental.model.property.Car;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class SearchCarsForm extends GenericCarForm {

    private Car.CarStatus status;

    private Long department;

    @NotNull(message = "Field 'country' cannot be empty")
    private Country country;

    private String plate;

    private Long mileageMin;

    private Long mileageMax;
}
