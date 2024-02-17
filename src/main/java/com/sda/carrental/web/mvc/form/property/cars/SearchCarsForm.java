package com.sda.carrental.web.mvc.form.property.cars;

import com.sda.carrental.web.mvc.form.validation.constraint.CarStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SearchCarsForm extends GenericCarForm {

    @CarStatus(canBeEmpty = true)
    private String status;

    private Long department;

    private String country;

    private String plate;

    private Long mileageMin;

    private Long mileageMax;
}
