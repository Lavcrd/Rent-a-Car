package com.sda.carrental.web.mvc.form.users;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class SearchEmployeesForm {
    @NotNull(message = "Failure: Department field is invalid")
    private Long department;

    private String name;

    private String surname;

    @NotNull(message = "Failure: Active-Expired field is invalid")
    private boolean isExpired;

    private String role;

    public SearchEmployeesForm() {
    }
}
