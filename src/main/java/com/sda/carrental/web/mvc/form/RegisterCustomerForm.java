package com.sda.carrental.web.mvc.form;

import com.sda.carrental.global.enums.Country;
import com.sda.carrental.web.mvc.form.validation.constraint.MatchingPassword;
import com.sda.carrental.web.mvc.form.validation.constraint.UniqueEmail;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
@MatchingPassword(message = "Passwords do not match!", targetFormClasses = {RegisterCustomerForm.class})
public class RegisterCustomerForm {

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

    @NotBlank(message = "E-mail field cannot be empty!")
    @Email(message = "E-mail has incorrect format")
    @UniqueEmail(message = "E-mail is taken")
    private String email;

    @NotBlank(message = "Password field cannot be empty!")
    @Size(min = 8, max = 64, message = "Passwords must be between 8 and 64 characters in length.")
    private String password;

    @NotBlank(message = "Repeat password field cannot be empty!")
    private String confirmPassword;
}
