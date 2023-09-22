package com.sda.carrental.service;

import com.sda.carrental.exceptions.IllegalActionException;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.ConstantValues;
import com.sda.carrental.global.enums.Country;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.operational.Retrieve;
import com.sda.carrental.model.property.Department;
import com.sda.carrental.repository.RetrieveRepository;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.ConfirmClaimForm;
import com.sda.carrental.web.mvc.form.SearchDepositsForm;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RetrieveService {
    private final ConstantValues cv;
    private final RetrieveRepository repository;
    private final ReservationService reservationService;
    private final RentService rentService;
    private final DepartmentService departmentService;


    public Retrieve findById(Long id) throws ResourceNotFoundException {
        return repository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional
    public HttpStatus createRetrieve(Long customerId, ConfirmClaimForm form) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            HttpStatus status = reservationService.handleReservationStatus(customerId, form.getReservationId(), Reservation.ReservationStatus.STATUS_COMPLETED, form.getMileage());
            if (status.equals(HttpStatus.ACCEPTED)) {
                repository.save(new Retrieve(form.getReservationId(), reservationService.findById(form.getReservationId()), rentService.findById(form.getReservationId()), cud.getId(), form.getDateTo(), form.getRemarks(), form.getMileage()));
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
    public HttpStatus handleRetrieve(Long customerId, Long departmentId, ConfirmClaimForm form) throws IllegalActionException {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (departmentService.departmentAccess(cud, departmentId).equals(HttpStatus.FORBIDDEN)) {
                throw new IllegalActionException();
            } else if (!departmentId.equals(form.getDepartmentId())) {
                reservationService.changeDestination(form.getReservationId(), form.getDepartmentId());
            }
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
        return repository.findAllUnresolvedByDepartments(departmentService.getDepartmentsByUserContext(cud));
    }

    public List<Retrieve> replaceDatesWithDeadlines(List<Retrieve> retrieves) {
        long days = (long) (Math.floor(cv.getRefundDepositDeadlineDays() / 5D) * 7 + (cv.getRefundDepositDeadlineDays() % 5));
        for (Retrieve r : retrieves) {
            r.setDateTo(r.getDateTo().plusDays(days));
        }
        return retrieves;
    }

    public List<Retrieve> findUnresolvedByUserContextAndForm(CustomUserDetails cud, SearchDepositsForm form) {
        String country;
        if (form.getCountry().equals(Country.COUNTRY_NONE)) {
            country = null;
        } else {
            country = form.getCountry().getCode();
        }

        List<Department> departments;
        if (form.getDepartment() == null) {
            departments = departmentService.getDepartmentsByUserContext(cud);
        } else {
            if (departmentService.departmentAccess(cud, form.getDepartment()).equals(HttpStatus.FORBIDDEN)) return Collections.emptyList();
            departments = List.of(departmentService.findDepartmentWhereId(form.getDepartment()));
        }

        return repository.findUnresolved(
                form.getName(), form.getSurname(),
                country, form.getPlate(),
                departments
        );
    }
}
