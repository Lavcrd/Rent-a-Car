package com.sda.carrental.service;


import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.property.Car;
import com.sda.carrental.model.property.Department;
import com.sda.carrental.model.property.PaymentDetails;
import com.sda.carrental.model.users.Customer;
import com.sda.carrental.repository.ReservationRepository;
import com.sda.carrental.web.mvc.form.IndexForm;
import com.sda.carrental.web.mvc.form.SearchCustomersForm;
import com.sda.carrental.web.mvc.form.SelectCarForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository repository;
    private final CarService carService;
    private final DepartmentService departmentService;
    private final PaymentDetailsService paymentDetailsService;
    private final VerificationService verificationService;

    public Reservation findById(Long id) throws ResourceNotFoundException{
        return repository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional
    public HttpStatus createReservation(Customer customer, SelectCarForm form) {
        try {
            IndexForm index = form.getIndexData();

            Car car = carService.findAvailableCar(index.getDateFrom(), index.getDateTo(), index.getDepartmentIdFrom(), form.getCarId());
            Department depRepFrom = departmentService.findDepartmentWhereId(index.getDepartmentIdFrom());
            Department depRepTo = departmentService.findDepartmentWhereId(index.getDepartmentIdTo());

            Reservation reservation = new Reservation(
                    customer, car,
                    depRepFrom, depRepTo,
                    index.getDateFrom(), index.getDateTo(),
                    index.getDateCreated());

            //payment method here linked with methods below this comment vvv + differ it for customer/employee process
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

    public List<Reservation> findCustomerReservations(Long customerId) {
        return repository
                .findAllByCustomerId(customerId);
    }

    public Reservation findCustomerReservation(Long customerId, Long reservationId) throws ResourceNotFoundException {
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
            Reservation r = findCustomerReservation(customerId, reservationId);
            Reservation.ReservationStatus currentStatus = r.getStatus();

            switch (status) {
                case STATUS_REFUNDED:
                    if (currentStatus == Reservation.ReservationStatus.STATUS_RESERVED) {
                        processRefundReservation(r, status);
                        return HttpStatus.ACCEPTED;
                    }
                    break;

                case STATUS_CANCELED:
                    if (currentStatus == Reservation.ReservationStatus.STATUS_PENDING ||
                            currentStatus == Reservation.ReservationStatus.STATUS_RESERVED) {
                        processCancelReservation(r, status);
                        return HttpStatus.ACCEPTED;
                    }
                    break;

                case STATUS_PROGRESS:
                    if (currentStatus == Reservation.ReservationStatus.STATUS_RESERVED) {
                        if (verificationService.getOptionalVerificationByCustomer(r.getCustomer().getId()).isEmpty()) {
                            return HttpStatus.PRECONDITION_REQUIRED;
                        }
                        Optional<PaymentDetails> payment = paymentDetailsService.getOptionalPaymentDetails(r.getId());
                        if (payment.isEmpty()) {
                            return HttpStatus.PAYMENT_REQUIRED;
                        }
                        processProgressReservation(r, status, payment.get());
                        return HttpStatus.ACCEPTED;
                    }
                    break;

                case STATUS_COMPLETED:
                    if (currentStatus == Reservation.ReservationStatus.STATUS_PROGRESS) {
                        processCompleteReservation(r, status);
                        return HttpStatus.ACCEPTED;
                    }
                    break;

                default:
                    break;
            }

            return HttpStatus.BAD_REQUEST;
        } catch (ResourceNotFoundException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.NOT_FOUND;
        } catch (RuntimeException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Transactional
    private void processRefundReservation(Reservation r, Reservation.ReservationStatus status) {
        paymentDetailsService.retractReservationPayment(r, status);
        updateReservationStatus(r, status);
    }

    @Transactional
    private void processCancelReservation(Reservation r, Reservation.ReservationStatus status) {
        paymentDetailsService.retractReservationPayment(r, status);
        updateReservationStatus(r, status);
    }

    @Transactional
    private void processProgressReservation(Reservation r, Reservation.ReservationStatus status, PaymentDetails payment) {
        paymentDetailsService.securePayment(payment);
        carService.updateCarStatus(r.getCar(), Car.CarStatus.STATUS_RENTED);
        updateReservationStatus(r, status);
    }

    @Transactional
    private void processCompleteReservation(Reservation r, Reservation.ReservationStatus status) {
        carService.updateCarStatus(r.getCar(), Car.CarStatus.STATUS_OPEN);
        carService.updateCarLocation(r.getCar(), r.getDepartmentBack().getId());
        updateReservationStatus(r, status);
    }


    public List<Reservation> findUserReservationsByDepartmentTake(Long customerId, Long departmentId) throws ResourceNotFoundException {
        try {
            return repository
                    .findAllByCustomerIdAndDepartmentTakeId(customerId, departmentId);
        } catch (RuntimeException err) {
            throw new ResourceNotFoundException();
        }
    }

    public List<Reservation> findUserReservationsByDepartmentBack(Long customerId, Long departmentId) throws ResourceNotFoundException {
        try {
            return repository
                    .findAllByCustomerIdAndDepartmentBackId(customerId, departmentId);
        } catch (RuntimeException err) {
            throw new ResourceNotFoundException();
        }
    }

    public List<Reservation> findDeparturesByDetails(SearchCustomersForm reservationsData) {
        return repository.findDeparturesByDetails(
                reservationsData.getCustomerName(), reservationsData.getCustomerSurname(),
                reservationsData.getDepartmentTake(), reservationsData.getDepartmentBack(),
                reservationsData.getDateFrom(), reservationsData.getDateTo(),
                reservationsData.getStatus());
    }

    public List<Reservation> findArrivalsByDetails(SearchCustomersForm reservationsData) {
        return repository.findArrivalsByDetails(
                reservationsData.getCustomerName(), reservationsData.getCustomerSurname(),
                reservationsData.getDepartmentTake(), reservationsData.getDepartmentBack(),
                reservationsData.getDateFrom(), reservationsData.getDateTo(),
                reservationsData.getStatus());
    }

    @Transactional
    public HttpStatus substituteCar(Long reservationId, Long customerId, Long carId) {
        try {
            Reservation r = findCustomerReservation(customerId, reservationId);
            Car c = carService.findAvailableCar(r.getDateFrom(), r.getDateTo(), r.getDepartmentTake().getId(), carId);
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

    @Transactional
    public boolean transferReservations(Customer mainCustomer, Customer usedCustomer) {
        try {
            List<Reservation> reservations = findCustomerReservations(usedCustomer.getId());
            for (Reservation reservation : reservations) {
                reservation.setCustomer(mainCustomer);
            }
            repository.saveAll(reservations);
            return true;
        } catch (RuntimeException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
    }

    public Reservation findActiveReservationByPlate(String plate) throws ResourceNotFoundException {
        return repository.findActiveReservationByPlate(plate).orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional
    public void changeDestination(Long reservationId, Long departmentId) throws ResourceNotFoundException {
        Reservation r = findById(reservationId);
        Department d = departmentService.findDepartmentWhereId(departmentId);
        r.setDepartmentBack(d);
        repository.save(r);
    }
}
