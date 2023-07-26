package com.sda.carrental.web.mvc.form;

import com.sda.carrental.global.enums.Country;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.*;

@Getter
@Setter
public class LocalReservationForm {
    @NotBlank(message = "Name field cannot be empty!")
    @Length(min = 1, max = 50, message = "Please enter a valid name.")
    private String name;

    @NotBlank(message = "Surname field cannot be empty!")
    @Length(min = 1, max = 50, message = "Please enter a valid surname.")
    private String surname;

    @NotBlank(message = "Contact field cannot be empty!")
    @Size(min=7, max=15, message="Incorrect contact number size")
    @Digits(integer = 15, fraction = 0, message="Incorrect contact number format")
    private String contactNumber;

    private Country country;

    @NotBlank(message = "City field cannot be empty!")
    @Length(min = 1, max = 50, message = "Please enter a valid city.")
    private String city;

    @NotBlank(message = "Address field cannot be empty!")
    @Length(min = 1, max = 50, message = "Please enter a valid address.")
    private String address;

    @Pattern(regexp = ".{4,}", message = "Must contain minimum 4 characters.")
    private String personalId;

    @Pattern(regexp = ".{4,}", message = "Must contain minimum 4 characters.")
    private String driverId;

    private SelectCarForm reservationForm;
}
