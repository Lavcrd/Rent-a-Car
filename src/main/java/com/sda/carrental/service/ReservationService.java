package com.sda.carrental.service;


import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.property.Car;
import com.sda.carrental.model.property.Department;
import com.sda.carrental.model.property.PaymentDetails;
import com.sda.carrental.model.users.Customer;
import com.sda.carrental.repository.ReservationRepository;
import com.sda.carrental.web.mvc.form.IndexForm;
import com.sda.carrental.web.mvc.form.SearchReservationsForm;
import com.sda.carrental.web.mvc.form.SelectCarForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository repository;
    private final CustomerService customerService;
    private final CarService carService;
    private final DepartmentService departmentService;
    private final PaymentDetailsService paymentDetailsService;
    private final VerificationService verificationService;

    @Transactional
    public HttpStatus createReservation(Long customerId, SelectCarForm form) {
        try {
            IndexForm index = form.getIndexData();

            Customer customer = customerService.findById(customerId);
            Car car = carService.findAvailableCar(index.getDateFrom(), index.getDateTo(), index.getDepartmentIdFrom(), form.getCarId());
            Department depRepFrom = departmentService.findDepartmentWhereId(index.getDepartmentIdFrom());
            Department depRepTo = departmentService.findDepartmentWhereId(index.getDepartmentIdTo());

            Reservation reservation = new Reservation(
                    customer, car,
                    depRepFrom, depRepTo,
                    index.getDateFrom(), index.getDateTo(),
                    index.getDateCreated());

            //payment method here linked with methods below this comment vvv
            reservation.setStatus(Reservation.ReservationStatus.STATUS_RESERVED);
            repository.save(reservation);
            // ^^^

            paymentDetailsService.createReservationPayment(reservation);

            return HttpStatus.CREATED;
        } catch (ResourceNotFoundException err) {
            err.printStackTrace();
            return HttpStatus.NOT_FOUND;
        } catch (Exception err) {
            err.printStackTrace();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public List<Reservation> getCustomerReservations(Long customerId) {
        return repository
                .findAllByCustomerId(customerId);
    }

    public Reservation getCustomerReservation(Long customerId, Long reservationId) {
        return repository
                .findByCustomerIdAndId(customerId, reservationId)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional
    private void updateReservationStatus(Reservation reservation, Reservation.ReservationStatus newStatus) {
        reservation.setStatus(newStatus);
        repository.save(reservation);
    }

    @Transactional
    public HttpStatus handleReservationStatus(Long customerId, Long reservationId, Reservation.ReservationStatus status) {
        try {
            Reservation r = getCustomerReservation(customerId, reservationId);

            if (status.equals(Reservation.ReservationStatus.STATUS_REFUNDED) && r.getStatus().equals(Reservation.ReservationStatus.STATUS_RESERVED)) {
                paymentDetailsService.retractReservationPayment(r, status);
                updateReservationStatus(r, status);
                return HttpStatus.ACCEPTED;
            } else if (status.equals(Reservation.ReservationStatus.STATUS_CANCELED) && (r.getStatus().equals(Reservation.ReservationStatus.STATUS_PENDING) || r.getStatus().equals(Reservation.ReservationStatus.STATUS_RESERVED))) {
                paymentDetailsService.retractReservationPayment(r, status);
                updateReservationStatus(r, status);
                return HttpStatus.ACCEPTED;
            } else if (status.equals(Reservation.ReservationStatus.STATUS_PROGRESS) && r.getStatus().equals(Reservation.ReservationStatus.STATUS_RESERVED)) {
                Optional<PaymentDetails> payment = paymentDetailsService.getOptionalPaymentDetails(r);
                if (verificationService.getOptionalVerificationByCustomer(r.getCustomer().getId()).isEmpty()) {
                    return HttpStatus.PRECONDITION_REQUIRED;
                }
                if (payment.isEmpty()) {
                    return HttpStatus.PAYMENT_REQUIRED;
                }
                paymentDetailsService.securePayment(payment.get());
                carService.updateCarStatus(r.getCar(), Car.CarStatus.STATUS_RENTED);
                updateReservationStatus(r, status);
                return HttpStatus.ACCEPTED;
            }
            return HttpStatus.BAD_REQUEST;
        } catch (ResourceNotFoundException err) {
            err.printStackTrace();
            return HttpStatus.NOT_FOUND;
        }
    }

    public List<Reservation> getUserReservationsByDepartmentTake(Long customerId, Long departmentId) {
        return repository
                .findAllByCustomerIdAndDepartmentTakeId(customerId, departmentId);
    }

    public List<Reservation> getUserReservationsByDepartmentBack(Long customerId, Long departmentId) {
        return repository
                .findAllByCustomerIdAndDepartmentBackId(customerId, departmentId);
    }

    public List<Reservation> findReservationsByDetails(SearchReservationsForm reservationsData) {
        return repository.findReservationByDetails(
                reservationsData.getCustomerName(), reservationsData.getCustomerSurname(),
                reservationsData.getDepartmentTake(), reservationsData.getDepartmentBack(),
                reservationsData.getDateFrom(), reservationsData.getDateTo(),
                reservationsData.getStatus());
    }

    @Transactional
    public HttpStatus substituteCar(Long reservationId, Long customerId, Long carId) {
        try {
            Reservation r = getCustomerReservation(customerId, reservationId);
            Car c = carService.findAvailableCar(r.getDateFrom(), r.getDateTo(), r.getDepartmentTake().getDepartmentId(), carId);
            r.setCar(c);
            repository.save(r);
            paymentDetailsService.adjustRequiredDeposit(r, c.getDepositValue());
            return HttpStatus.ACCEPTED;
        } catch (RuntimeException err) {
            return HttpStatus.NOT_FOUND;
        }
    }

    public boolean hasActiveReservations(Long customerId) {
        return !repository.findAllActiveByCustomerId(customerId).isEmpty();
    }
}
