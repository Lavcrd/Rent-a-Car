package com.sda.rentacar.web.mvc.form.property.departments;

import com.sda.rentacar.web.mvc.form.common.ConfirmationForm;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.*;

@Getter
@Setter
@ToString
public class RegisterDepartmentForm extends ConfirmationForm {
    @NotNull
    private Long country;

    @NotBlank(message = "Failure: City field cannot be empty!")
    @Length(min = 1, max = 50, message = "Failure: Please enter a valid city.")
    private String city;

    @NotBlank(message = "Failure: Street field cannot be empty!")
    @Length(min = 1, max = 50, message = "Failure: Please enter a valid street.")
    private String street;

    @NotBlank(message = "Failure: Building number field cannot be empty!")
    @Length(min = 1, max = 15, message = "Failure: Please enter a valid building number.")
    private String building;

    @NotBlank(message = "Failure: Postcode field cannot be empty!")
    @Length(min = 1, max = 15, message = "Failure: Please enter a valid postcode.")
    private String postcode;

    @NotEmpty(message = "Failure: E-mail field cannot be empty.")
    @Email(message = "Failure: E-mail has invalid format.")
    private String email;

    @NotBlank(message = "Failure: Contact field cannot be empty!")
    @Pattern(regexp = "[\\s\\d]{6,30}+$", message="Failure: Incorrect contact number format.")
    private String contact;
}
