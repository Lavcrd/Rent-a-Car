package com.sda.carrental.web.mvc.form.property.cars;

import com.sda.carrental.web.mvc.form.common.ConfirmationForm;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SplitCarBaseForm extends ConfirmationForm {
    @NotNull(message = "Failure: No pattern selected")
    @Positive(message = "Failure: Unexpected value")
    private Long pattern;

    @NotEmpty(message = "Failure: No cars selected")
    private List<Long> cars;
}