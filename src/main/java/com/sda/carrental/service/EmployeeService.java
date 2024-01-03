package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.users.Employee;
import com.sda.carrental.repository.EmployeeRepository;
import com.sda.carrental.web.mvc.form.users.SearchEmployeesForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository repository;

    public Employee findEmployeeById(Long id) {
        return repository.findEmployeeById(id).orElseThrow(ResourceNotFoundException::new);
    }

    public List<Employee> findByForm(SearchEmployeesForm form) {
        return repository.findAllByForm(form.getName(), form.getSurname(), form.getDepartment(), form.isExpired());
    }
}
