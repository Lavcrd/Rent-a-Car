package com.sda.rentacar.service;

import com.sda.rentacar.exceptions.IllegalActionException;
import com.sda.rentacar.exceptions.ResourceNotFoundException;
import com.sda.rentacar.model.operational.Reservation;
import com.sda.rentacar.model.operational.Retrieve;
import com.sda.rentacar.model.property.car.Car;
import com.sda.rentacar.model.property.department.Department;
import com.sda.rentacar.repository.RetrieveRepository;
import com.sda.rentacar.service.auth.CustomUserDetails;
import com.sda.rentacar.web.mvc.form.operational.ConfirmClaimForm;
import com.sda.rentacar.web.mvc.form.operational.SearchArchiveForm;
import com.sda.rentacar.web.mvc.form.property.payments.SearchDepositsForm;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RetrieveService {
    private final SettingsService settingsService;
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
                        departmentService.findById(form.getDepartmentId()), form.getMileage()));
            }
            return status;
        } catch (DataAccessException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        } catch (ResourceNotFoundException e) {
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
            carService.retrieveCar(rentService.findById(form.getReservationId()).getCar(), departmentService.findById(form.getDepartmentId()), form.getMileage());
            return createRetrieve(customerId, form);
        } catch (IllegalActionException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.BAD_REQUEST;
        } catch (ResourceNotFoundException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.NOT_FOUND;
        }
    }

    public List<Retrieve> findAllUnresolvedByUserContext(CustomUserDetails cud) {
        return repository.findAllUnresolvedByDepartments(employeeService.getDepartmentsByUserContext(cud));
    }

    public List<Retrieve> replaceDatesWithDeadlines(List<Retrieve> retrieves) {
        long deadlineTimeframe = settingsService.getInstance().getRefundDepositDeadlineDays();
        long days = (long) (Math.floor(deadlineTimeframe / 5D) * 7 + (deadlineTimeframe % 5));
        for (Retrieve r : retrieves) {
            r.setDateTo(r.getDateTo().plusDays(days));
        }
        return retrieves;
    }

    public List<Retrieve> findUnresolvedByUserContextAndForm(CustomUserDetails cud, SearchDepositsForm form) {
        List<Department> departments;
        if (form.getDepartment() == null) {
            departments = employeeService.getDepartmentsByUserContext(cud);
        } else {
            if (employeeService.departmentAccess(cud, form.getDepartment()).equals(HttpStatus.FORBIDDEN))
                return Collections.emptyList();
            departments = List.of(departmentService.findById(form.getDepartment()));
        }

        return repository.findUnresolved(
                form.getName(), form.getSurname(),
                form.getCountry(), form.getPlate(),
                departments
        );
    }

    public List<Retrieve> findRetrievalsByCar(Car car, Integer limit) {
        return repository.findRetrievalsByCar(car, Pageable.ofSize(limit));
    }

    public List<Retrieve> findByUserContextAndForm(CustomUserDetails cud, SearchArchiveForm form) {
        try {
            List<Department> departments;
            if (form.getDepartment() == null) {
                departments = employeeService.getDepartmentsByUserContext(cud);
            } else {
                if (employeeService.departmentAccess(cud, form.getDepartment()).equals(HttpStatus.FORBIDDEN))
                    return Collections.emptyList();
                departments = List.of(departmentService.findById(form.getDepartment()));
            }

            return repository.findRetrievedByCriteria(
                    form.getName(), form.getSurname(),
                    form.getCountry(), form.getPlate(),
                    form.getDateFrom(), form.getDateTo(),
                    departments, form.isArrival()
            );
        } catch (RuntimeException e) {
            return Collections.emptyList();
        }
    }

    public void mapStatistics(Map<String, Double> map, Long departmentId, LocalDate dateFrom, LocalDate dateTo) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Object[] dr = (Object[]) repository.getDepartmentRetrieveStatistics(departmentId, dateFrom, dateTo);
        Object[] ar = (Object[]) repository.getAccountRetrieveStatistics(cud.getId(), dateFrom, dateTo);
        Object[] gr = (Object[]) repository.getGlobalRetrieveStatistics(dateFrom, dateTo);

        mapDepartmentStatistics(map, dr);
        mapAccountStatistics(map, ar);
        mapGlobalStatistics(map, gr);
    }

    private void mapDepartmentStatistics(Map<String, Double> map, Object[] dr) {
        double retrieveCompleted = dr[0] != null ? ((BigInteger) dr[0]).doubleValue() : 0.0;
        double retrieveDuration = dr[1] != null ? ((BigDecimal) dr[1]).doubleValue() : 0.0;
        double retrieveMileage = dr[2] != null ? ((BigDecimal) dr[2]).doubleValue() : 0.0;

        map.put("retrieve_completed", retrieveCompleted);
        map.put("retrieve_avg_duration", retrieveDuration);
        map.put("retrieve_avg_mileage", retrieveMileage);
    }

    private void mapAccountStatistics(Map<String, Double> map, Object[] ar) {
        double accountRetrieveCompleted = ar[0] != null ? ((BigDecimal) ar[0]).doubleValue() : 0.0;
        double accountRetrieveDuration = ar[1] != null ? ((BigDecimal) ar[1]).doubleValue() : 0.0;
        double accountRetrieveMileage = ar[2] != null ? ((BigDecimal) ar[2]).doubleValue() : 0.0;

        map.put("account_retrieve_completed", accountRetrieveCompleted);
        map.put("account_retrieve_avg_duration", accountRetrieveDuration);
        map.put("account_retrieve_avg_mileage", accountRetrieveMileage);
    }

    private void mapGlobalStatistics(Map<String, Double> map, Object[] gr) {
        double globalRetrieveCompleted = gr[0] != null ? ((BigDecimal) gr[0]).doubleValue() : 0.0;
        double globalRetrieveDuration = gr[1] != null ? ((BigDecimal) gr[1]).doubleValue() : 0.0;
        double globalRetrieveMileage = gr[2] != null ? ((BigDecimal) gr[2]).doubleValue() : 0.0;

        map.put("global_retrieve_completed", globalRetrieveCompleted);
        map.put("global_retrieve_avg_duration", globalRetrieveDuration);
        map.put("global_retrieve_avg_mileage", globalRetrieveMileage);
    }
}
