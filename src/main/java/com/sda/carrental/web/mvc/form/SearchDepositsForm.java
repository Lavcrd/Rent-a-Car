package com.sda.carrental.web.mvc.form;

import com.sda.carrental.global.enums.Country;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;


@Getter
@Setter
@NoArgsConstructor
public class SearchDepositsForm {
    private String name;

    private String surname;

    private Long department;

    @NotNull(message = "Field 'country' cannot be empty")
    private Country country;

    private String plate;
}
