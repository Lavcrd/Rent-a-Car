package com.sda.rentacar.web.mvc.form.property.cars;

import com.sda.rentacar.web.mvc.form.common.ConfirmationForm;
import com.sda.rentacar.web.mvc.form.validation.constraint.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Positive;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class RegisterCarBaseForm extends ConfirmationForm {

    @ImageFile
    private MultipartFile image;

    private String brand;

    private String model;

    @CarType
    private String type;

    @PastOrPresentYear
    private Integer year;

    @Positive(message = "Failure: Seats value must be positive.")
    private Integer seats;

    @Positive(message = "Failure: Price value must be positive.")
    private Double price;

    @Positive(message = "Failure: Deposit value must be positive.")
    private Double deposit;
}
