package com.sda.carrental.web.mvc.form;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class SearchDepositsForm {
    @NotBlank(message = "Field 'department' cannot be empty")
    private String customerName;

    private String customerSurname;

    private Long departmentBack;

    @NotBlank(message = "Field 'starting date' cannot be empty")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateFrom;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateTo;

    public SearchDepositsForm(LocalDate dateFrom, LocalDate dateTo) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }
}
