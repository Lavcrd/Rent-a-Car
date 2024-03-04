package com.sda.carrental.web.mvc.form.users.employee;

import com.sda.carrental.model.property.department.Department;
import com.sda.carrental.web.mvc.form.common.ConfirmationForm;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateDepartmentsForm extends ConfirmationForm {

    private List<Department> departments;

    public UpdateDepartmentsForm(List<Department> departments) {
        this.departments = departments;
    }
}