package com.sda.carrental.web.mvc.form.property.departments;

import com.sda.carrental.model.property.Department;
import com.sda.carrental.web.mvc.form.common.ConfirmationForm;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
public class UpdateDepartmentForm extends ConfirmationForm {
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


    public UpdateDepartmentForm(Department department) {
        this.city = department.getCity();
        this.street = department.getStreet();
        this.building = department.getBuilding();
        this.postcode = department.getPostcode();
    }
}
