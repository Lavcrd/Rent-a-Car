package com.sda.rentacar.web.mvc.form.property.cars;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class SubstituteCarBaseFilterForm extends GenericCarForm {
    private Long departmentId;

    public SubstituteCarBaseFilterForm(Long departmentId) {
        this.departmentId = departmentId;
    }
}
