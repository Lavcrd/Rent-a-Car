package com.sda.carrental.web.mvc.form;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class SubstituteCarFilterForm extends GenericCarForm {
    private LocalDate dateFrom;

    private LocalDate dateTo;

    private Long departmentId;

    public SubstituteCarFilterForm(LocalDate dateFrom, LocalDate dateTo, Long departmentId) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.departmentId = departmentId;
    }
}
