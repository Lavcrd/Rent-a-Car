package com.sda.rentacar.web.mvc.form.property.departments;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class RefreshOverviewForm {
    @NotNull
    private Long departmentId;

    public RefreshOverviewForm(Long departmentId) {
        this.departmentId = departmentId;
    }
}
