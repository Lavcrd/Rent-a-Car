package com.sda.carrental.web.mvc.form.property.cars;

import com.sda.carrental.web.mvc.form.validation.constraint.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class RegisterCarBaseForm {

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

    @NotEmpty(message = "Failure: Field cannot be empty")
    @CurrentPassword(message = "Failure: Provided password confirmation is not valid")
    private String currentPassword;
}
