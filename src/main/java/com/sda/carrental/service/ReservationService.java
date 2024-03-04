package com.sda.carrental.service;


import com.sda.carrental.exceptions.IllegalActionException;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.property.department.Department;
import com.sda.carrental.model.property.car.CarBase;
import com.sda.carrental.model.users.Customer;
import com.sda.carrental.repository.ReservationRepository;
import com.sda.carrental.web.mvc.form.operational.IndexForm;
import com.sda.carrental.web.mvc.form.users.customer.SearchCustomersForm;
import com.sda.carrental.web.mvc.form.operational.ReservationForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository repository;
    private final CarBaseService carBaseService;
    private final DepartmentService departmentService;
    private final PaymentDetailsService paymentDetailsService;
    private final VerificationService verificationService;

    public Reservation findById(Long id) throws ResourceNotFoundException {
        return repository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional
    public HttpStatus createReservation(Customer customer, ReservationForm form) {
        try {
            IndexForm index = form.getIndexData();

            if (index.getDateFrom().isAfter(index.getDateTo())) throw new ResourceNotFoundException();
            CarBase carBase = carBaseService.findById(form.getCarBaseId());
            Department depRepFrom = departmentService.findById(index.getDepartmentIdFrom());
            Department depRepTo = departmentService.findById(index.getDepartmentIdTo());

            Reservation reservation = new Reservation(
                    customer, carBase,
                    depRepFrom, depRepTo,
                    index.getDateFrom(), index.getDateTo(),
                    LocalDate.now());

            //Assume employee has to register transaction to another employee responsible for finances
            //Assume customer pays at reservation process - until that moment status should be ReservationStatus.STATUS_PENDING by default
            //Payment method here linked with methods below this comment vvv
            reservation.setStatus(Reservation.ReservationStatus.STATUS_RESERVED);
            repository.save(reservation);
            // ^^^

            //Placeholder payment values
            long days = reservation.getDateFrom().until(reservation.getDateTo(), ChronoUnit.DAYS) + 1;

            Double exchange = reservation.getDepartmentTake().getCountry().getExchange();
            Double multiplier = exchange * reservation.getDepartmentTake().getMultiplier();

            double payment = days * reservation.getCarBase().getPriceDay() * multiplier;
            double deposit = reservation.getCarBase().getDepositValue() * exchange;
            //^^^
            paymentDetailsService.createReservationPayment(reservation, payment, deposit);

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

                        processProgressReservation(r, status);
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
    private void processProgressReservation(Reservation r, Reservation.ReservationStatus status) {
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

    public List<Reservation> findDeparturesByDetails(SearchCustomersForm reservationsData, Reservation.ReservationStatus status) {
        return repository.findDeparturesByDetails(
                reservationsData.getCustomerName(), reservationsData.getCustomerSurname(),
                reservationsData.getPrimaryDepartment(), reservationsData.getSecondaryDepartment(),
                reservationsData.getDateFrom(), reservationsData.getDateTo(),
                status);
    }

    public List<Reservation> findArrivalsByDetails(SearchCustomersForm reservationsData, Reservation.ReservationStatus status) {
        return repository.findArrivalsByDetails(
                reservationsData.getCustomerName(), reservationsData.getCustomerSurname(),
                reservationsData.getPrimaryDepartment(), reservationsData.getSecondaryDepartment(),
                reservationsData.getDateFrom(), reservationsData.getDateTo(),
                status);
    }

    @Transactional
    public HttpStatus substituteCarBase(Reservation r, Long carBaseId) {
        try {
            if (!r.getStatus().equals(Reservation.ReservationStatus.STATUS_RESERVED)) throw new IllegalArgumentException();
            Department department = r.getDepartmentTake();
            CarBase cb = carBaseService.findAvailableCarBaseInDepartment(carBaseId, department.getId());
            Double deposit = cb.getDepositValue() * department.getMultiplier() * department.getCountry().getExchange();

            r.setCarBase(cb);
            repository.save(r);
            paymentDetailsService.adjustRequiredDeposit(r, deposit);
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

    public boolean isValidRequest(IndexForm indexForm, Long carBaseId, LocalDateTime requestTime1, LocalDateTime requestTime2) {
        try {
            //If request calls for different department, dptFrom should not be equal to dptTo - nor the reverse way should be allowed
            if (indexForm.isDifferentDepartment() == indexForm.getDepartmentIdFrom().equals(indexForm.getDepartmentIdTo())) return false;

            //Request data should be correct chronologically
            if (!requestTime2.isAfter(requestTime1) || indexForm.getDateFrom().isAfter(indexForm.getDateTo())) return false;

            //Request was done within 1 hour - to prevent refreshing || processing after long time - session in config lasts 60m without activity - might be reconsidered
            if (!LocalDateTime.now().isBefore(requestTime2.plusHours(1).plusMinutes(30))) return false;

            //Checks if exists with exceptions from respective services
            CarBase carBase = carBaseService.findById(carBaseId);
            Department departmentFrom = departmentService.findById(indexForm.getDepartmentIdFrom());
            departmentService.findById(indexForm.getDepartmentIdTo());

            //Checks if Car Base is available within provided request assuming overbooking is not allowed
            List<CarBase> carBaseList = carBaseService.findAvailableCarBasesInCountry(
                    indexForm.getDateFrom(),
                    indexForm.getDateTo(),
                    departmentFrom.getCountry());

            return carBaseList.contains(carBase);
        } catch (RuntimeException err) {
            return false;
        }
    }
}
