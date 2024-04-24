package com.sda.rentacar.web.mvc.form.users.employee;

import com.sda.rentacar.model.users.Employee;
import com.sda.rentacar.web.mvc.form.common.ConfirmationForm;
import com.sda.rentacar.web.mvc.form.validation.constraint.UserRole;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
public class UpdateEmployeeForm extends ConfirmationForm {

    @NotBlank(message = "Failure: Name field cannot be empty!")
    @Length(min = 1, max = 50, message = "Please enter a valid name.")
    String name;

    @NotBlank(message = "Failure: Surname field cannot be empty!")
    @Length(min = 1, max = 50, message = "Please enter a valid surname.")
    String surname;

    @UserRole(canBeUnselected = false)
    String role;

    @NotBlank(message = "Failure: Contact field cannot be empty")
    @Pattern(regexp = "^\\+{0,1}[\\s\\d]{6,30}+$", message = "Incorrect contact number format")
    String contactNumber;

    public UpdateEmployeeForm(Employee employee) {
        this.name = employee.getName();
        this.surname = employee.getSurname();
        this.role = employee.getRole().name();
        this.contactNumber = employee.getContactNumber();
    }
}
