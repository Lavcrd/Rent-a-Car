package com.sda.rentacar.service.mappers;


import com.sda.rentacar.exceptions.ResourceNotFoundException;
import com.sda.rentacar.model.property.department.Department;
import com.sda.rentacar.model.users.Employee;
import com.sda.rentacar.web.mvc.form.users.employee.RegisterEmployeeForm;

import java.util.ArrayList;
import java.util.List;

public class EmployeeMapper {
    public static Employee toRegisteredEntity(RegisterEmployeeForm form, Department department) throws ResourceNotFoundException {
        List<Department> dptList = new ArrayList<>();
        dptList.add(department);
        return new Employee(form.getName(), form.getSurname(), form.getPersonalId(), dptList, form.getValidDate(), form.getContactNumber());
    }
}
