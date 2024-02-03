package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.enums.Role;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.property.Department;
import com.sda.carrental.model.property.car.Car;
import com.sda.carrental.model.users.Employee;
import com.sda.carrental.model.users.User;
import com.sda.carrental.repository.EmployeeRepository;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.users.employee.SearchEmployeesForm;
import com.sda.carrental.web.mvc.form.users.employee.UpdateEmployeeForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository repository;
    private final DepartmentService departmentService;
    private final ReservationService reservationService;

    public Employee findById(Long id) throws ResourceNotFoundException {
        return repository.findEmployeeById(id).orElseThrow(ResourceNotFoundException::new);
    }

    public List<Employee> findByForm(SearchEmployeesForm form) {
        Role role = null;
        if (!form.getRole().isBlank()) role = Role.valueOf(form.getRole());
        return repository.findAllByForm(form.getName(), form.getSurname(), form.getDepartment(), form.isExpired(), role);
    }

    public List<Role> getEmployeeEnums() {
        return List.of(Role.ROLE_EMPLOYEE, Role.ROLE_MANAGER, Role.ROLE_COORDINATOR, Role.ROLE_DIRECTOR);
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

    public List<Department> getDepartmentsByUserContext(CustomUserDetails cud) {
        if (!cud.getType().equals(User.Type.TYPE_EMPLOYEE)) return Collections.emptyList();
        if (cud.getAuthorities().contains(new SimpleGrantedAuthority(Role.ROLE_ADMIN.name()))) {
            return departmentService.findAll();
        }
        return findById(cud.getId()).getDepartments();
    }

    public HttpStatus departmentAccess(CustomUserDetails cud, Long departmentId) throws ResourceNotFoundException {
        Department department = departmentService.findDepartmentWhereId(departmentId);
        if (getDepartmentsByUserContext(cud).contains(department)) {
            return HttpStatus.ACCEPTED;
        }
        return HttpStatus.FORBIDDEN;
    }

    public boolean isSupervisorOf(CustomUserDetails cud, Employee subEmployee) {
        try {
            if (departmentAccess(cud, subEmployee.getDepartments().get(0).getId()).equals(HttpStatus.FORBIDDEN)) {
                return false;
            }
            return findById(cud.getId()).getRole().ordinal() > subEmployee.getRole().ordinal();
        } catch (RuntimeException e) {
            return false;
        }
    }

    public boolean hasNoAccessToCustomerData(CustomUserDetails cud, Long customerId, Long departmentId) {
        try {
            if (departmentAccess(cud, departmentId).equals(HttpStatus.ACCEPTED)) {
                boolean hasNoReservationsDepTake = reservationService.findUserReservationsByDepartmentTake(customerId, departmentId).isEmpty();
                boolean hasNoReservationsDepBack = reservationService.findUserReservationsByDepartmentBack(customerId, departmentId).isEmpty();

                return hasNoReservationsDepTake && hasNoReservationsDepBack;
            }
            return true;
        } catch (ResourceNotFoundException err) {
            return true;
        }
    }

    public boolean hasNoAccessToCustomerOperation(CustomUserDetails cud, Long customerId, Long operationId) {
        try {
            Reservation r = reservationService.findCustomerReservation(customerId, operationId);

            HttpStatus accessDepFrom = departmentAccess(cud, r.getDepartmentTake().getId());
            HttpStatus accessDepTo = departmentAccess(cud, r.getDepartmentBack().getId());

            return accessDepFrom == HttpStatus.FORBIDDEN && accessDepTo == HttpStatus.FORBIDDEN;
        } catch (ResourceNotFoundException err) {
            return true;
        }
    }

    public boolean hasNoAccessToProperty(CustomUserDetails cud, Object object) {
        try {
            if (object instanceof Department d) {
                HttpStatus accessDep = departmentAccess(cud, d.getId());
                return accessDep == HttpStatus.FORBIDDEN;
            } else if (object instanceof Car c) {
                HttpStatus accessDep = departmentAccess(cud, c.getDepartment().getId());
                return accessDep == HttpStatus.FORBIDDEN;
            } else {
                return true;
            }
        } catch (ResourceNotFoundException err) {
            return true;
        }
    }

    public HttpStatus setLock(Long employeeId, LocalDate date) throws ResourceNotFoundException {
        Employee employee = findById(employeeId);
        if (date.isBefore(LocalDate.now())) {
            return HttpStatus.BAD_REQUEST;
        }
        employee.setTerminationDate(date);
        repository.save(employee);
        return HttpStatus.ACCEPTED;
    }

    @Transactional
    public HttpStatus setDepartments(Long employeeId, List<Department> departments) {
        try {
            Employee employee = findById(employeeId);
            if ((employee.getRole().equals(Role.ROLE_EMPLOYEE) || employee.getRole().equals(Role.ROLE_MANAGER))
                    && departments.size() > 1) {
                return HttpStatus.PRECONDITION_FAILED;
            }

            employee.setDepartments(departments);
            repository.save(employee);
            return HttpStatus.ACCEPTED;
        } catch (RuntimeException e) {
            return HttpStatus.BAD_GATEWAY;
        }
    }
}
