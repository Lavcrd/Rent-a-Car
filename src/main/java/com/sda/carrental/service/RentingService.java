package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.operational.Renting;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.repository.RentingRepository;
import com.sda.carrental.service.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentingService {
    private final RentingRepository repository;
    private final ReservationService reservationService;

    @Transactional
    public HttpStatus createRent(Long customerId, Long reservationId, String remarks) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            HttpStatus status = reservationService.handleReservationStatus(customerId, reservationId, Reservation.ReservationStatus.STATUS_PROGRESS);
            if (status.equals(HttpStatus.ACCEPTED)) {
                repository.save(new Renting(cud.getId(), reservationId, remarks));
            }
            return status;
        } catch (DataAccessException err) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public Object findById(Long id) throws ResourceNotFoundException {
        return repository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }
}
