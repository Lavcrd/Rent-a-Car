package com.sda.carrental.web.mvc.form;

import com.sda.carrental.web.mvc.form.validation.constraint.ValidCountry;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SearchDepositsForm {
    private String name;

    private String surname;

    private Long department;

    @ValidCountry
    private String country;

    private String plate;
}
