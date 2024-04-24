package com.sda.rentacar.web.mvc.form.users.employee;

import com.sda.rentacar.web.mvc.form.validation.constraint.UserRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class SearchEmployeesForm {
    @NotNull(message = "Failure: Department field is invalid")
    private Long department;

    private String name;

    private String surname;

    @NotNull(message = "Failure: Active-Expired field is invalid")
    private boolean isExpired;

    @UserRole
    private String role;
}
