package com.sda.carrental.web.mvc.form.property.departments;

import com.sda.carrental.web.mvc.form.validation.constraint.CorrectChronology;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@CorrectChronology(message = "Failure: Incorrect date order")
public class RefreshStatisticsForm {
    @NotNull
    private Long departmentId;

    @NotNull
    private Long previousId;

    @NotNull
    @PastOrPresent(message = "Failure: Invalid date.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateFrom;

    @NotNull
    @PastOrPresent(message = "Failure: Invalid date.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateTo;

    public RefreshStatisticsForm(Long departmentId, LocalDate dateFrom, LocalDate dateTo) {
        this.departmentId = departmentId;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }
}
