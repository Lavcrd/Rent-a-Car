package com.sda.carrental.service.mappers;


import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.property.department.Department;
import com.sda.carrental.model.users.Employee;
import com.sda.carrental.web.mvc.form.users.employee.RegisterEmployeeForm;

import java.util.ArrayList;
import java.util.List;

public class EmployeeMapper {
    public static Employee toRegisteredEntity(RegisterEmployeeForm form, Department department) throws ResourceNotFoundException {
        List<Department> dptList = new ArrayList<>();
        dptList.add(department);
        return new Employee(form.getName(), form.getSurname(), form.getPersonalId(), dptList, form.getValidDate(), form.getContactNumber());
    }
}
