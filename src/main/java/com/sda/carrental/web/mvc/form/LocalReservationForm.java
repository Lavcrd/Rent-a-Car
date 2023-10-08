package com.sda.carrental.web.mvc.form;

import com.sda.carrental.global.enums.Country;
import com.sda.carrental.web.mvc.form.validation.constraint.ValidCountry;
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
    @Pattern(regexp = "^\\+{0,1}[\\s\\d]{6,30}+$", message="Incorrect contact number format and/or length")
    private String contactNumber;

    @ValidCountry
    private Country country;

    @Pattern(regexp = "\\S{8,16}", message = "Personal IDN must be in range of 8 to 16 characters")
    private String personalId;

    @Pattern(regexp = "\\S{4,16}", message = "Driver ID must be in range of 4 to 16 characters")
    private String driverId;

    private SelectCarForm reservationForm;
}
