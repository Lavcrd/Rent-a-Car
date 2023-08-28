package com.sda.carrental.service;

import com.sda.carrental.exceptions.IllegalActionException;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.ConstantValues;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.operational.Returning;
import com.sda.carrental.repository.ReturningRepository;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.ConfirmClaimForm;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReturningService {
    private final ConstantValues cv;
    private final ReturningRepository repository;
    private final ReservationService reservationService;
    private final RentingService rentingService;
    private final DepartmentService departmentService;


    public Object findById(Long id) throws ResourceNotFoundException {
        return repository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional
    public HttpStatus createReturn(Long customerId, Long reservationId, LocalDate dateTo, String remarks) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            HttpStatus status = reservationService.handleReservationStatus(customerId, reservationId, Reservation.ReservationStatus.STATUS_COMPLETED);
            if (status.equals(HttpStatus.ACCEPTED)) {
                repository.save(new Returning(reservationId, reservationService.findById(reservationId), rentingService.findById(reservationId), cud.getId(), dateTo, remarks));
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
    public HttpStatus handleReturn(Long customerId, Long reservationId, Long departmentId, ConfirmClaimForm form) throws IllegalActionException {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (departmentService.departmentAccess(cud, departmentId).equals(HttpStatus.FORBIDDEN)) {
                throw new IllegalActionException();
            } else if (!departmentId.equals(form.getDepartmentId())) {
                reservationService.changeDestination(reservationId, form.getDepartmentId());
            }
            return createReturn(customerId, reservationId, form.getDateTo(), form.getRemarks());
        } catch (IllegalActionException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.BAD_REQUEST;
        } catch (ResourceNotFoundException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.NOT_FOUND;
        }
    }

    public List<Returning> findAllUnresolvedByUserContext(CustomUserDetails cud) {
        return repository.findAllUnresolvedByDepartments(departmentService.getDepartmentsByUserContext(cud));
    }

    public List<Returning> replaceDatesWithDeadlines(List<Returning> returns) {
        long days = (long) (Math.floor(cv.getRefundDepositDeadlineDays() / 5D) * 7 + (cv.getRefundDepositDeadlineDays() % 5));
        for (Returning r : returns) {
            r.setDateTo(r.getDateTo().plusDays(days));
        }
        return returns;
    }
}
