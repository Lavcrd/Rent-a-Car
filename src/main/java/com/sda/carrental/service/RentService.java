package com.sda.carrental.service;

import com.sda.carrental.exceptions.IllegalActionException;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.operational.Rent;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.property.car.Car;
import com.sda.carrental.repository.RentRepository;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.ConfirmRentalForm;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RentService {
    private final RentRepository repository;
    private final ReservationService reservationService;
    private final CarService carService;

    public Rent findById(Long id) throws ResourceNotFoundException {
        return repository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional
    public HttpStatus createRent(Long customerId, ConfirmRentalForm form) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            HttpStatus status = reservationService.handleReservationStatus(customerId, form.getReservationId(), Reservation.ReservationStatus.STATUS_PROGRESS);
            if (status.equals(HttpStatus.ACCEPTED)) {
                Reservation r = reservationService.findById(form.getReservationId());
                Car c = carService.findAvailableCar(form.getCarId(), r.getDepartmentTake().getId());

                if (carService.isCarUnavailable(c)) throw new IllegalActionException();
                carService.updateCarStatus(c, Car.CarStatus.STATUS_RENTED);

                repository.save(new Rent(form.getReservationId(), r, c, cud.getId(), form.getRemarks(), form.getDateFrom(), form.getMileage()));
            }
            return status;
        } catch (DataAccessException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        } catch (IllegalActionException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.CONFLICT;
        } catch (ResourceNotFoundException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.NOT_FOUND;
        }
    }

    public Optional<Rent> findActiveByCar(Car car) {
        return repository.findActiveByCar(car);
    }

    public Rent findActiveOperationByCarPlate(String plate) throws ResourceNotFoundException {
        return repository.findActiveOperationByCarPlate(plate).orElseThrow(ResourceNotFoundException::new);
    }

}
