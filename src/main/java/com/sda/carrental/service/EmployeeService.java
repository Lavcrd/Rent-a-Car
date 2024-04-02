package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.Encryption;
import com.sda.carrental.global.enums.Role;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.property.department.Department;
import com.sda.carrental.model.property.car.Car;
import com.sda.carrental.model.users.Employee;
import com.sda.carrental.model.users.User;
import com.sda.carrental.repository.EmployeeRepository;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.service.mappers.EmployeeMapper;
import com.sda.carrental.web.mvc.form.users.employee.RegisterEmployeeForm;
import com.sda.carrental.web.mvc.form.users.employee.SearchEmployeesForm;
import com.sda.carrental.web.mvc.form.users.employee.UpdateEmployeeForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository repository;
    private final AdminService adminService;
    private final DepartmentService departmentService;
    private final ReservationService reservationService;
    private final CredentialsService credentialsService;
    private final EntityManager entityManager;
    private final Encryption e;

    public Employee findById(Long id) throws RuntimeException {
        Employee employee = repository.findEmployeeById(id).orElseThrow(ResourceNotFoundException::new);
        entityManager.detach(employee);
        return decrypt(employee);
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

            save(employee);
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
        Employee employee = findById(cud.getId());
        return employee.getDepartments();
    }

    public HttpStatus departmentAccess(CustomUserDetails cud, Long departmentId) throws RuntimeException {
        Department department = departmentService.findById(departmentId);
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
        } catch (RuntimeException e) {
            return true;
        }
    }

    public boolean hasNoAccessToCustomerOperation(CustomUserDetails cud, Long customerId, Long operationId) {
        try {
            Reservation r = reservationService.findCustomerReservation(customerId, operationId);

            HttpStatus accessDepFrom = departmentAccess(cud, r.getDepartmentTake().getId());
            HttpStatus accessDepTo = departmentAccess(cud, r.getDepartmentBack().getId());

            return accessDepFrom == HttpStatus.FORBIDDEN && accessDepTo == HttpStatus.FORBIDDEN;
        } catch (RuntimeException e) {
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
        } catch (RuntimeException e) {
            return true;
        }
    }

    public HttpStatus setLock(Long employeeId, LocalDate date) throws RuntimeException {
        Employee employee = findById(employeeId);
        if (date.isBefore(LocalDate.now())) {
            return HttpStatus.BAD_REQUEST;
        }
        employee.setTerminationDate(date);
        save(employee);
        return HttpStatus.ACCEPTED;
    }

    @Transactional
    public HttpStatus setDepartments(Long employeeId, List<Department> departments) {
        try {
            Employee employee = findById(employeeId);
            if ((employee.getRole().equals(Role.ROLE_EMPLOYEE) || employee.getRole().equals(Role.ROLE_MANAGER))
                    && departments.size() > 1) {
                return HttpStatus.PRECONDITION_FAILED;
            } else if (departments.isEmpty()) {
                return HttpStatus.BAD_REQUEST;
            }

            employee.setDepartments(departments);
            save(employee);
            return HttpStatus.ACCEPTED;
        } catch (RuntimeException e) {
            return HttpStatus.BAD_GATEWAY;
        }
    }

    public boolean hasMinimumAuthority(CustomUserDetails cud, Role role) {
        try {
            if (adminService.hasAdminAuthority(cud)) return true;

            return findById(cud.getId()).getRole().ordinal() >= role.ordinal();
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Transactional
    public HttpStatus register(RegisterEmployeeForm form) {
        try {
            Employee employee = EmployeeMapper.toRegisteredEntity(form, departmentService.findById(form.getDepartment()));
            save(employee);
            credentialsService.createCredentials(employee.getId(), form.getUsername(), form.getEmployeePassword());
            return HttpStatus.CREATED;
        } catch (RuntimeException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Transactional
    public HttpStatus delete(Long id) {
        try {
            if (hasPresence(id)) {
                return HttpStatus.PRECONDITION_FAILED;
            }

            Employee employee = findById(id);
            repository.delete(employee);
            credentialsService.deleteCredentials(id);
            return HttpStatus.OK;
        } catch (RuntimeException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public boolean hasPresence(Long id) {
        return repository.hasPresence(id).contains(true);
    }

    @Transactional
    public HttpStatus assignDepartment(Long id, Department department) {
        try {
            Employee employee = findById(id);
            List<Department> departments = employee.getDepartments();
            if (departments.contains(department)) return HttpStatus.ACCEPTED;

            departments.add(department);
            employee.setDepartments(departments);

            save(employee);
            return HttpStatus.ACCEPTED;
        } catch (RuntimeException e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Transactional
    private void save(Employee employee) throws RuntimeException {
        repository.save(encrypt(employee));
    }

    private Employee encrypt(Employee employee) throws RuntimeException {
        employee.setPersonalId(e.encrypt(employee.getPersonalId()));
        employee.setContactNumber(e.encrypt(employee.getContactNumber()));
        return employee;
    }

    private Employee decrypt(Employee employee) throws RuntimeException {
        employee.setPersonalId(e.decrypt(employee.getPersonalId()));
        employee.setContactNumber(e.decrypt(employee.getContactNumber()));
        return employee;
    }
}
