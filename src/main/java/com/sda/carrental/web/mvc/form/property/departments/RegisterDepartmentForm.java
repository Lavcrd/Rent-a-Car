package com.sda.carrental.web.mvc.form.property.departments;

import com.sda.carrental.web.mvc.form.common.ConfirmationForm;
import com.sda.carrental.web.mvc.form.validation.constraint.ValidCountry;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.*;

@Getter
@Setter
@ToString
public class RegisterDepartmentForm extends ConfirmationForm {
    @ValidCountry(canBeUnselected = false)
    private String country;

    @NotBlank(message = "Failure: City field cannot be empty!")
    @Length(min = 1, max = 50, message = "Failure: Please enter a valid city.")
    private String city;

    @NotBlank(message = "Failure: Address field cannot be empty!")
    @Length(min = 1, max = 50, message = "Failure: Please enter a valid address.")
    private String address;

    @NotBlank(message = "Failure: Postcode field cannot be empty!")
    @Length(min = 1, max = 15, message = "Failure: Please enter a valid postcode.")
    private String postcode;

    @NotEmpty(message = "Failure: E-mail field cannot be empty.")
    @Email(message = "Failure: E-mail has invalid format.")
    private String email;

    @NotBlank(message = "Failure: Contact field cannot be empty!")
    @Pattern(regexp = "^\\+{0,1}[\\s\\d]{6,30}+$", message="Failure: Incorrect contact number format.")
    private String contact;
}
