package com.sda.carrental.web.mvc.form;

import com.sda.carrental.web.mvc.form.validation.constraint.CurrentPassword;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ConfirmClaimForm {
    @NotNull
    private Long departmentId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateTo;

    @NotEmpty(message = "Field must contain a statement or description")
    private String remarks;

    @NotEmpty(message = "Field cannot be empty")
    @CurrentPassword(message = "Provided password confirmation is not valid")
    private String currentPassword;

    public ConfirmClaimForm(Long departmentId, LocalDate dateTo) {
        this.departmentId = departmentId;
        this.dateTo = dateTo;
    }
}
