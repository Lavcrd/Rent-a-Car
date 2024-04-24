package com.sda.rentacar.web.mvc.form.users.employee;

import com.sda.rentacar.web.mvc.form.common.ConfirmationForm;
import com.sda.rentacar.web.mvc.form.validation.constraint.MatchingPassword;
import com.sda.rentacar.web.mvc.form.validation.constraint.UniqueUsername;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Getter
@Setter
@ToString
@MatchingPassword(message = "Passwords do not match!", targetFormClasses = {RegisterEmployeeForm.class})
public class RegisterEmployeeForm extends ConfirmationForm {
    @NotBlank(message = "Name field cannot be empty!")
    @Length(min = 1, max = 50, message = "Please enter a valid name.")
    private String name;

    @NotBlank(message = "Surname field cannot be empty!")
    @Length(min = 1, max = 50, message = "Please enter a valid surname.")
    private String surname;

    @NotBlank(message = "Personal ID field cannot be empty!")
    @Length(min = 1, max = 50, message = "Please enter valid personal ID.")
    private String personalId;

    @NotBlank(message = "Contact field cannot be empty!")
    @Pattern(regexp = "^\\+{0,1}[\\s\\d]{6,30}+$", message="Incorrect contact number format")
    private String contactNumber;

    @NotBlank(message = "E-mail field cannot be empty!")
    @Email(message = "E-mail has incorrect format")
    @UniqueUsername(message = "E-mail is taken")
    private String username;

    @NotBlank(message = "Password field cannot be empty!")
    @Size(min = 8, max = 64, message = "Passwords must be between 8 and 64 characters in length.")
    private String employeePassword;

    @NotBlank(message = "Password field cannot be empty!")
    @Size(min = 8, max = 64, message = "Passwords must be between 8 and 64 characters in length.")
    private String employeePasswordRe;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @FutureOrPresent(message = "Failure: Invalid date value.")
    private LocalDate validDate;

    @NotNull(message = "Failure: Department field is invalid")
    private Long department;
}
