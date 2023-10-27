package com.sda.carrental.web.mvc.form;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class SubstituteCarFilterForm extends GenericCarForm {
    private Long departmentId;

    public SubstituteCarFilterForm(Long departmentId) {
        this.departmentId = departmentId;
    }
}
