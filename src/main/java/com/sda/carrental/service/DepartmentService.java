package com.sda.carrental.service;


import com.sda.carrental.global.enums.Country;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.property.Department;
import com.sda.carrental.model.users.User;
import com.sda.carrental.repository.DepartmentRepository;
import com.sda.carrental.service.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final CoordinatorService coordinatorService;
    private final ManagerService managerService;
    private final EmployeeService employeeService;

    public List<Department> findAll() {
        return StreamSupport.stream(departmentRepository.findAll().spliterator(), false)
                .collect(toList());
    }

    public List<Department> findAllWhereCountry(Country country) {
        return departmentRepository.findDepartmentsByCountry(country);
    }

    public Department findAllWhereCountryAndHq(Country country) {
        return departmentRepository.findDepartmentByCountryAndHq(country, true).orElse(new Department(Country.COUNTRY_PL, "—", "—", "—", "—", "—", true));
    }

    public Department findDepartmentWhereId(long id) {
        return departmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    }

    public List<Department> getDepartmentsByRole(CustomUserDetails cud) {
        if (cud.getAuthorities().contains(new SimpleGrantedAuthority(User.Roles.ROLE_EMPLOYEE.name()))) {
            return List.of(employeeService.findEmployeeById(cud.getId()).getDepartment());
        }

        if (cud.getAuthorities().contains(new SimpleGrantedAuthority(User.Roles.ROLE_MANAGER.name()))) {
            return List.of(managerService.findManagerById(cud.getId()).getDepartment());
        }

        if (cud.getAuthorities().contains(new SimpleGrantedAuthority(User.Roles.ROLE_COORDINATOR.name()))) {
            return coordinatorService.findCoordinatorById(cud.getId()).getDepartments();
        }

        if (cud.getAuthorities().contains(new SimpleGrantedAuthority(User.Roles.ROLE_ADMIN.name()))) {
            return findAll();
        }

        return new LinkedList<>();
    }

    public HttpStatus departmentAccess(CustomUserDetails cud, Long departmentId) {
        Department department = findDepartmentWhereId(departmentId);
        if (getDepartmentsByRole(cud).contains(department)) {
            return HttpStatus.ACCEPTED;
        } else {
            return HttpStatus.FORBIDDEN;
        }
    }
}
