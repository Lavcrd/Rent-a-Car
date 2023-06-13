package com.sda.carrental.service;


import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.property.Car;
import com.sda.carrental.model.property.Department;
import com.sda.carrental.model.property.PaymentDetails;
import com.sda.carrental.model.users.Customer;
import com.sda.carrental.model.users.User;
import com.sda.carrental.repository.ReservationRepository;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.IndexForm;
import com.sda.carrental.web.mvc.form.SelectCarForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final UserService userService;
    private final CustomerService customerService;
    private final CarService carService;
    private final DepartmentService departmentService;
    private final PaymentDetailsService paymentDetailsService;
    private final VerificationService verificationService;
    private final RentingService rentingService;

    @Transactional
    public HttpStatus createReservation(@RequestBody CustomUserDetails cud, @RequestBody SelectCarForm form) {
        try {
            IndexForm index = form.getIndexData();

            User user = userService.findByUsername(cud.getUsername());
            Car car = carService.findAvailableCar(index.getDateFrom(), index.getDateTo(), index.getDepartmentIdFrom(), form.getCarId());
            Department depRepFrom = departmentService.findDepartmentWhereId(index.getDepartmentIdFrom());
            Department depRepTo = departmentService.findDepartmentWhereId(index.getDepartmentIdTo());

            Reservation reservation = new Reservation(
                    (Customer) user, car,
                    depRepFrom, depRepTo,
                    index.getDateFrom(), index.getDateTo(),
                    index.getDateCreated());

            //payment method here linked with methods below this comment vvv
            reservation.setStatus(Reservation.ReservationStatus.STATUS_RESERVED);
            reservationRepository.save(reservation);
            // ^^^

            paymentDetailsService.createReservationPayment(reservation);

            return HttpStatus.CREATED;
        } catch (ResourceNotFoundException err) {
            err.printStackTrace();
            return HttpStatus.NOT_FOUND;
        } catch (Error err) {
            err.printStackTrace();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public List<Reservation> getUserReservations(@RequestBody String username) {
        return reservationRepository
                .findAllByUser(userService.findByUsername(username));
    }

    public Reservation getCustomerReservation(@RequestBody Long customerId, Long reservationId) {
        return reservationRepository
                .findByUserAndId(customerService.findById(customerId), reservationId)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional
    private void updateReservationStatus(Reservation reservation, Reservation.ReservationStatus newStatus) {
        reservation.setStatus(newStatus);
        reservationRepository.save(reservation);
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
                if (verificationService.getOptionalVerificationByCustomer(r.getCustomer()).isEmpty()) {
                    return HttpStatus.PRECONDITION_REQUIRED;
                }
                if (payment.isEmpty()) {
                    return HttpStatus.PAYMENT_REQUIRED;
                }
                paymentDetailsService.securePayment(payment.get());
                updateReservationStatus(r, status);
                rentingService.createRent(r);
                return HttpStatus.ACCEPTED;
            }
            return HttpStatus.BAD_REQUEST;
        } catch (ResourceNotFoundException err) {
            err.printStackTrace();
            return HttpStatus.NOT_FOUND;
        }
    }

    public List<Reservation> getUserReservationsByDepartmentTake(String username, Long departmentId) {
        return reservationRepository
                .findAllByUsernameAndDepartmentId(username, departmentId);
    }

    @Transactional
    public HttpStatus substituteCar(Long reservationId, Long customerId, Long carId) {
        try {
            Reservation r = getCustomerReservation(customerId, reservationId);
            r.setCar(carService.findAvailableCar(r.getDateFrom(), r.getDateTo(), r.getDepartmentTake().getDepartmentId(), carId));
            reservationRepository.save(r);
            return HttpStatus.ACCEPTED;
        } catch (RuntimeException err) {
            return HttpStatus.NOT_FOUND;
        }
    }
}
