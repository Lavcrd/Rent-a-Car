package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.enums.Role;
import com.sda.carrental.model.users.Employee;
import com.sda.carrental.repository.EmployeeRepository;
import com.sda.carrental.web.mvc.form.users.SearchEmployeesForm;
import com.sda.carrental.web.mvc.form.users.employee.UpdateEmployeeForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository repository;

    public Employee findById(Long id) throws ResourceNotFoundException {
        return repository.findEmployeeById(id).orElseThrow(ResourceNotFoundException::new);
    }

    public List<Employee> findByForm(SearchEmployeesForm form) {
        Role role = null;
        if (!form.getRole().isBlank()) role = Role.valueOf(form.getRole());
        return repository.findAllByForm(form.getName(), form.getSurname(), form.getDepartment(), form.isExpired(), role);
    }

    public List<Role> getEmployeeEnums() {
        return List.of(Role.ROLE_EMPLOYEE, Role.ROLE_MANAGER, Role.ROLE_COORDINATOR);
    }

    @Transactional
    public HttpStatus updateDetails(Employee employee, UpdateEmployeeForm form) {
        try {
            employee.setName(form.getName());
            employee.setSurname(form.getSurname());
            employee.setRole(Role.valueOf(form.getRole()));
            employee.setContactNumber(form.getContactNumber());

            repository.save(employee);
            return HttpStatus.OK;
        } catch (RuntimeException e) {
            return HttpStatus.BAD_GATEWAY;
        }
    }
}
