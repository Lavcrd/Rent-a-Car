package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.operational.Returning;
import com.sda.carrental.repository.ReturningRepository;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.ConfirmReturnForm;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReturningService {
    private final ReturningRepository repository;
    private final ReservationService reservationService;


    public Object findById(Long id) throws ResourceNotFoundException {
        return repository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional
    public HttpStatus createReturn(Long customerId, Long reservationId, ConfirmReturnForm form) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            HttpStatus status = reservationService.handleReservationStatus(customerId, reservationId, Reservation.ReservationStatus.STATUS_COMPLETED);
            if (status.equals(HttpStatus.ACCEPTED)) {
                repository.save(new Returning(reservationId, cud.getId(), form.getDateTo(), form.getRemarks()));
            }
            return status;
        } catch (DataAccessException err) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
