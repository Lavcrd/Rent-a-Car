package com.sda.carrental.service;


import com.sda.carrental.global.enums.Country;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.property.Department;
import com.sda.carrental.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository repository;

    public List<Department> findAll() {
        return StreamSupport.stream(repository.findAll().spliterator(), false)
                .collect(toList());
    }

    public List<Department> findAllWhereCountry(Country country) {
        return repository.findDepartmentsByCountry(country);
    }

    public Department findAllWhereCountryAndHq(Country country) {
        return repository.findDepartmentByCountryAndHq(country, true).orElse(new Department(Country.COUNTRY_PL, "—", "—", "—", "—", "—", true));
    }

    public Department findDepartmentWhereId(long id) throws ResourceNotFoundException {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    }
}
