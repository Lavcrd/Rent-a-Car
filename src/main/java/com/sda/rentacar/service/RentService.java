package com.sda.rentacar.service;

import com.sda.rentacar.exceptions.IllegalActionException;
import com.sda.rentacar.exceptions.ResourceNotFoundException;
import com.sda.rentacar.model.operational.Rent;
import com.sda.rentacar.model.operational.Reservation;
import com.sda.rentacar.model.property.car.Car;
import com.sda.rentacar.repository.RentRepository;
import com.sda.rentacar.service.auth.CustomUserDetails;
import com.sda.rentacar.web.mvc.form.operational.ConfirmRentalForm;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
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
public class RentService {
    private final RentRepository repository;
    private final ReservationService reservationService;
    private final PaymentDetailsService paymentDetailsService;
    private final CarService carService;

    public Rent findById(Long id) throws ResourceNotFoundException {
        return repository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional
    public HttpStatus createRent(Long customerId, ConfirmRentalForm form) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Reservation r = reservationService.findById(form.getReservationId());

            HttpStatus response = paymentDetailsService.securePayment(r, form.isIgnoredStatus());

            if (response.equals(HttpStatus.ACCEPTED)) {
                HttpStatus status = reservationService.handleReservationStatus(customerId, form.getReservationId(), Reservation.ReservationStatus.STATUS_PROGRESS);
                Car c = carService.findAvailableCar(form.getCarId(), r.getDepartmentTake().getId());

                if (carService.isCarUnavailable(c)) throw new IllegalActionException();
                carService.updateCarStatus(c, Car.CarStatus.STATUS_RENTED);

                repository.save(new Rent(form.getReservationId(), r, c, cud.getId(), form.getRemarks(), form.getDateFrom(), form.getMileage()));
                return status;
            }
            return response;
        } catch (DataAccessException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        } catch (IllegalActionException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.CONFLICT;
        } catch (ResourceNotFoundException e) {
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

    public List<Rent> findIncomingByDepartment(Long departmentId) {
        try {
            return repository.findIncomingByDepartmentAndStatus(departmentId, Reservation.ReservationStatus.STATUS_PROGRESS);
        } catch (RuntimeException e) {
            return Collections.emptyList();
        }
    }

}
