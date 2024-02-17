package com.sda.carrental.web.mvc.form.property.payments;

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

    private String country;

    private String plate;
}
