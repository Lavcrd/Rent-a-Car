package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.users.Employee;
import com.sda.carrental.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository repository;

    public Employee findEmployeeById(Long id) {
        return repository.findEmployeeById(id).orElseThrow(ResourceNotFoundException::new);
    }
}
