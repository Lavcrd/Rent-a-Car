package com.sda.rentacar.web.mvc.form.users.customer;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
public class FindVerifiedForm {
    @NotNull
    private Long country;

    @Pattern(regexp = "\\S{8,16}", message = "Personal IDN must be in range of 8 to 16 characters")
    private String personalId;
}
