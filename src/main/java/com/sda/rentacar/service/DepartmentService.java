package com.sda.rentacar.service;

import com.sda.rentacar.exceptions.ResourceNotFoundException;
import com.sda.rentacar.model.property.department.Country;
import com.sda.rentacar.model.property.department.Department;
import com.sda.rentacar.repository.DepartmentRepository;
import com.sda.rentacar.web.mvc.form.property.departments.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository repository;
    private final CountryService countryService;

    public List<Department> findAll() {
        return StreamSupport
                .stream(repository.findAll().spliterator(), false)
                .collect(toList());
    }

    public List<Department> findAllWhereCountry(Country country) {
        return repository.findDepartmentsByCountry(country);
    }

    public Department findAllWhereCountryAndHq(Country country) {
        return repository.findDepartmentByCountryAndHq(country, true)
                .orElse(new Department(countryService.placeholder(), "—", "—", "", "—", "—", "—", true));
    }

    public Department findById(long id) throws ResourceNotFoundException {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    }

    public List<Department> findByForm(SearchDepartmentsForm form) {
        return repository.findAllByForm(form.getCity(), form.getStreet(), form.getBuilding() , form.getPostcode(), form.isActive(), form.isHeadquarter(), form.getCountry());
    }

    @Transactional
    public Department register(RegisterDepartmentForm form) throws RuntimeException {
        Country country = countryService.findById(form.getCountry());
        Department department = new Department(country, form.getCity(), form.getStreet(), form.getBuilding(), form.getPostcode(), form.getEmail(), form.getContact(), false);

        return repository.save(department);
    }

    public boolean hasPresence(Long id) {
        List<BigInteger> results = repository.hasPresence(id);
        for (BigInteger result : results) {
            if (result.intValue() == 1) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    public HttpStatus updateDetails(Long id, UpdateDepartmentForm form) {
        try {
            Department department = findById(id);

            department.setCity(form.getCity());
            department.setStreet(form.getStreet());
            department.setBuilding(form.getBuilding());
            department.setPostcode(form.getPostcode());

            repository.save(department);
            return HttpStatus.ACCEPTED;
        } catch (RuntimeException e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Transactional
    public HttpStatus updateContacts(Long id, UpdateContactsForm form) {
        try {
            Department department = findById(id);

            department.setContact(form.getContact());
            department.setEmail(form.getEmail());

            repository.save(department);
            return HttpStatus.ACCEPTED;
        } catch (RuntimeException e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Transactional
    public HttpStatus updateActivity(Long id) {
        try {
            Department department = findById(id);
            department.setActive(!department.isActive());

            repository.save(department);
            return HttpStatus.ACCEPTED;
        } catch (RuntimeException e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Transactional
    public HttpStatus updateHQ(Long id) {
        try {
            Department department = findById(id);
            department.setHq(!department.isHq());

            repository.save(department);
            return HttpStatus.ACCEPTED;
        } catch (RuntimeException e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Transactional
    public HttpStatus delete(Long id) {
        try {
            if (hasPresence(id)) {
                return HttpStatus.PRECONDITION_FAILED;
            }
            repository.deleteById(id);
            return HttpStatus.ACCEPTED;
        } catch (RuntimeException e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Transactional
    public HttpStatus updateMultiplier(Long id, UpdateMultiplierForm form) {
        try {
            Department department = findById(id);

            department.setMultiplier(form.getMultiplier());

            repository.save(department);
            return HttpStatus.ACCEPTED;
        } catch (RuntimeException e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
