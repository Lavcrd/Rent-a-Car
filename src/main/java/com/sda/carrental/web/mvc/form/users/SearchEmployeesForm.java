package com.sda.carrental.web.mvc.form.users;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchEmployeesForm {
    private Long department;

    private String name;

    private String surname;

    private boolean isExpiry;

    private String role;

    public SearchEmployeesForm() {
    }
}
