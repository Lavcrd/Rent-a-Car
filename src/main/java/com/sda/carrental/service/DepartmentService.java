package com.sda.carrental.service;


import com.sda.carrental.exceptions.IllegalActionException;
import com.sda.carrental.global.enums.Country;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.property.Department;
import com.sda.carrental.repository.DepartmentRepository;
import com.sda.carrental.web.mvc.form.property.departments.RegisterDepartmentForm;
import com.sda.carrental.web.mvc.form.property.departments.SearchDepartmentsForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return repository.findDepartmentByCountryAndHq(country, true).orElse(new Department(Country.COUNTRY_NONE, "—", "—", "—", "—", "—", true));
    }

    public Department findById(long id) throws ResourceNotFoundException {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    }

    public List<Department> findByForm(SearchDepartmentsForm form) {
        Country country = null;
        if (!form.getCountry().isBlank() && !form.getCountry().equals(Country.COUNTRY_NONE.name())) country = Country.valueOf(form.getCountry());
        return repository.findAllByForm(form.getCity(), form.getAddress(), form.getPostcode(), form.isActive(), form.isHeadquarter(), country);

    }

    @Transactional
    public HttpStatus register(RegisterDepartmentForm form) {
        try {
            Country country = Country.valueOf(form.getCountry());
            if (country.equals(Country.COUNTRY_NONE)) throw new IllegalActionException();

            Department department = new Department(country, form.getCity(), form.getAddress(), form.getPostcode(), form.getEmail(), form.getContact(), false);
            repository.save(department);

            return HttpStatus.CREATED;
        } catch (RuntimeException e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
     }
}
