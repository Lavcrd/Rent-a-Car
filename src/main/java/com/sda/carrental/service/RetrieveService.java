package com.sda.carrental.service;

import com.sda.carrental.exceptions.IllegalActionException;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.ConstantValues;
import com.sda.carrental.global.enums.Country;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.operational.Retrieve;
import com.sda.carrental.model.property.car.Car;
import com.sda.carrental.model.property.Department;
import com.sda.carrental.repository.RetrieveRepository;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.operational.ConfirmClaimForm;
import com.sda.carrental.web.mvc.form.operational.SearchArchiveForm;
import com.sda.carrental.web.mvc.form.property.payments.SearchDepositsForm;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RetrieveService {
    private final ConstantValues cv;
    private final RetrieveRepository repository;
    private final ReservationService reservationService;
    private final RentService rentService;
    private final DepartmentService departmentService;
    private final CarService carService;
    private final EmployeeService employeeService;


    public Optional<Retrieve> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public HttpStatus createRetrieve(Long customerId, ConfirmClaimForm form) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            HttpStatus status = reservationService.handleReservationStatus(customerId, form.getReservationId(), Reservation.ReservationStatus.STATUS_COMPLETED);
            if (status.equals(HttpStatus.ACCEPTED)) {
                repository.save(new Retrieve(
                        form.getReservationId(), rentService.findById(form.getReservationId()),
                        cud.getId(), form.getDateTo(), form.getRemarks(),
                        departmentService.findDepartmentWhereId(form.getDepartmentId()), form.getMileage()));
            }
            return status;
        } catch (DataAccessException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        } catch (ResourceNotFoundException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.NOT_FOUND;
        }
    }

    @Transactional
    public HttpStatus handleRetrieve(Long customerId, Long departmentId, ConfirmClaimForm form) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (employeeService.departmentAccess(cud, departmentId).equals(HttpStatus.FORBIDDEN)) {
                throw new IllegalActionException();
            }
            carService.retrieveCar(rentService.findById(form.getReservationId()).getCar(), departmentService.findDepartmentWhereId(form.getDepartmentId()), form.getMileage());
            return createRetrieve(customerId, form);
        } catch (IllegalActionException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.BAD_REQUEST;
        } catch (ResourceNotFoundException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.NOT_FOUND;
        }
    }

    public List<Retrieve> findAllUnresolvedByUserContext(CustomUserDetails cud) {
        return repository.findAllUnresolvedByDepartments(employeeService.getDepartmentsByUserContext(cud));
    }

    public List<Retrieve> replaceDatesWithDeadlines(List<Retrieve> retrieves) {
        long days = (long) (Math.floor(cv.getRefundDepositDeadlineDays() / 5D) * 7 + (cv.getRefundDepositDeadlineDays() % 5));
        for (Retrieve r : retrieves) {
            r.setDateTo(r.getDateTo().plusDays(days));
        }
        return retrieves;
    }

    public List<Retrieve> findUnresolvedByUserContextAndForm(CustomUserDetails cud, SearchDepositsForm form) {
        Country formCountry = Country.valueOf(form.getCountry());
        String country;
        if (formCountry.equals(Country.COUNTRY_NONE)) {
            country = null;
        } else {
            country = formCountry.getCode();
        }

        List<Department> departments;
        if (form.getDepartment() == null) {
            departments = employeeService.getDepartmentsByUserContext(cud);
        } else {
            if (employeeService.departmentAccess(cud, form.getDepartment()).equals(HttpStatus.FORBIDDEN))
                return Collections.emptyList();
            departments = List.of(departmentService.findDepartmentWhereId(form.getDepartment()));
        }

        return repository.findUnresolved(
                form.getName(), form.getSurname(),
                country, form.getPlate(),
                departments
        );
    }

    public List<Retrieve> findRetrievalsByCar(Car car, Integer limit) {
        return repository.findRetrievalsByCar(car, Pageable.ofSize(limit));
    }

    public List<Retrieve> findByUserContextAndForm(CustomUserDetails cud, SearchArchiveForm form) {
        try {
            Country formCountry = Country.valueOf(form.getCountry());
            String country;
            if (formCountry.equals(Country.COUNTRY_NONE)) {
                country = null;
            } else {
                country = formCountry.getCode();
            }

            List<Department> departments;
            if (form.getDepartment() == null) {
                departments = employeeService.getDepartmentsByUserContext(cud);
            } else {
                if (employeeService.departmentAccess(cud, form.getDepartment()).equals(HttpStatus.FORBIDDEN))
                    return Collections.emptyList();
                departments = List.of(departmentService.findDepartmentWhereId(form.getDepartment()));
            }

            return repository.findRetrievedByCriteria(
                    form.getName(), form.getSurname(),
                    country, form.getPlate(),
                    form.getDateFrom(), form.getDateTo(),
                    departments, form.isArrival()
            );
        } catch (RuntimeException err) {
            return Collections.emptyList();
        }
    }
}
