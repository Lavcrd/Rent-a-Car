package com.sda.carrental.service;


import com.sda.carrental.global.enums.Country;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.enums.Role;
import com.sda.carrental.model.property.Department;
import com.sda.carrental.model.users.User;
import com.sda.carrental.repository.DepartmentRepository;
import com.sda.carrental.service.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository repository;
    private final EmployeeService employeeService;

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

    public List<Department> getDepartmentsByUserContext(CustomUserDetails cud) {
        if (!cud.getType().equals(User.Type.TYPE_EMPLOYEE)) return Collections.emptyList();
        if (cud.getAuthorities().contains(new SimpleGrantedAuthority(Role.ROLE_ADMIN.name()))) {
            return findAll();
        }
        return employeeService.findEmployeeById(cud.getId()).getDepartments();
    }

    public HttpStatus departmentAccess(CustomUserDetails cud, Long departmentId) throws ResourceNotFoundException {
        Department department = findDepartmentWhereId(departmentId);
        if (getDepartmentsByUserContext(cud).contains(department)) {
            return HttpStatus.ACCEPTED;
        }
        return HttpStatus.FORBIDDEN;
    }
}
