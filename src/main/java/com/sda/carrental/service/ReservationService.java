package com.sda.carrental.service;


import com.sda.carrental.exceptions.IllegalActionException;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.property.Department;
import com.sda.carrental.model.property.PaymentDetails;
import com.sda.carrental.model.property.car.CarBase;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository repository;
    private final CarService carService;
    private final CarBaseService carBaseService;
    private final DepartmentService departmentService;
    private final PaymentDetailsService paymentDetailsService;
    private final VerificationService verificationService;

    public Reservation findById(Long id) throws ResourceNotFoundException {
        return repository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    public boolean isChronologyValid(LocalDate dateFrom, LocalDate dateTo, LocalDate dateCreated) {
        return !dateFrom.isAfter(dateTo) && dateCreated.isEqual(LocalDate.now());
    }

    @Transactional
    public HttpStatus createReservation(Customer customer, SelectCarForm form) {
        try {
            IndexForm index = form.getIndexData();

            if (!isChronologyValid(index.getDateFrom(), index.getDateTo(), index.getDateCreated())) throw new ResourceNotFoundException();
            CarBase carBase = carBaseService.findById(form.getCarBaseId());
            Department depRepFrom = departmentService.findDepartmentWhereId(index.getDepartmentIdFrom());
            Department depRepTo = departmentService.findDepartmentWhereId(index.getDepartmentIdTo());

            Reservation reservation = new Reservation(
                    customer, carBase,
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
            return HttpStatus.NOT_FOUND;
        } catch (Exception err) {
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
        updateReservationStatus(r, status);
    }

    @Transactional
    private void processCompleteReservation(Reservation r, Reservation.ReservationStatus status) {
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
    public HttpStatus substituteCarBase(Long reservationId, Long customerId, Long carBaseId) {
        try {
            Reservation r = findCustomerReservation(customerId, reservationId);
            if (!r.getStatus().equals(Reservation.ReservationStatus.STATUS_RESERVED)) throw new IllegalArgumentException();
            CarBase cb = carBaseService.findAvailableCarBaseInDepartment(carBaseId, r.getDepartmentTake().getId());
            r.setCarBase(cb);
            repository.save(r);
            paymentDetailsService.adjustRequiredDeposit(r, cb.getDepositValue());
            return HttpStatus.ACCEPTED;
        } catch (IllegalActionException err) {
            return HttpStatus.CONFLICT;
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
}
