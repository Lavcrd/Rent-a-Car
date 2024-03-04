package com.sda.carrental.web.mvc.form.property.departments;

import com.sda.carrental.model.property.department.Department;
import com.sda.carrental.web.mvc.form.common.ConfirmationForm;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@ToString
public class UpdateContactsForm extends ConfirmationForm {
    @NotEmpty(message = "Failure: E-mail field cannot be empty.")
    @Email(message = "Failure: E-mail has invalid format.")
    private String email;

    @NotBlank(message = "Failure: Contact field cannot be empty!")
    @Pattern(regexp = "^\\+{0,1}[\\s\\d]{6,30}+$", message="Failure: Incorrect contact number format.")
    private String contact;

    public UpdateContactsForm(Department department) {
        this.email = department.getEmail();
        this.contact = department.getContact();
    }
}
