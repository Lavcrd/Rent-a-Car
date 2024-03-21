package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.Encryption;
import com.sda.carrental.global.Utility;
import com.sda.carrental.model.property.department.Country;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.users.Customer;
import com.sda.carrental.model.users.auth.Verification;
import com.sda.carrental.repository.CustomerRepository;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.service.mappers.CustomerMapper;
import com.sda.carrental.web.mvc.form.operational.LocalReservationForm;
import com.sda.carrental.web.mvc.form.operational.ReservationForm;
import com.sda.carrental.web.mvc.form.users.customer.FindVerifiedForm;
import com.sda.carrental.web.mvc.form.users.customer.RegisterCustomerForm;
import com.sda.carrental.web.mvc.form.users.customer.SearchCustomersForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository repository;
    private final CredentialsService credentialsService;
    private final VerificationService verificationService;
    private final CountryService countryService;
    private final ReservationService reservationService;
    private final EmployeeService employeeService;
    private final Encryption e;
    private final Utility u;

    public Customer findById(Long customerId) throws RuntimeException {
        return decrypt(repository.findById(customerId).orElseThrow(ResourceNotFoundException::new));
    }

    @Transactional(rollbackFor = {Exception.class})
    public HttpStatus createCustomer(RegisterCustomerForm form) {
        try {
            Customer customer = CustomerMapper.toRegisteredEntity(form);
            save(customer);
            credentialsService.createCredentials(customer.getId(), form.getUsername(), form.getPassword());
            return HttpStatus.CREATED;
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    private Customer redactCustomer(Customer customer) {
        customer.setName("—");
        customer.setSurname("—");
        customer.setContactNumber(u.generateRandomString(10));
        customer.setTerminationDate(LocalDate.now());
        customer.setStatus(Customer.Status.STATUS_DELETED);

        return customer;
    }

    @Transactional
    public HttpStatus deleteCustomer(Long customerId, boolean hasNoReservations) {
        try {
            Customer customer = findById(customerId);
            verificationService.deleteVerification(customerId);
            if (hasNoReservations) {
                repository.delete(customer);
            } else {
                repository.save(redactCustomer(customer));
            }
            return HttpStatus.OK;
        } catch (ResourceNotFoundException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.NOT_FOUND;
        } catch (RuntimeException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Transactional
    public HttpStatus unverifyCustomer(Long customerId) {
        if (reservationService.hasActiveReservations(customerId)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.CONFLICT;
        }
        return verificationService.deleteVerification(customerId);
    }

    @Transactional
    public Customer createGuest(LocalReservationForm form) throws RuntimeException {
        Customer customer = CustomerMapper.toGuestEntity(form);
        return save(customer);
    }

    public Customer findCustomerByVerification(FindVerifiedForm form) throws RuntimeException {
        Customer customer = repository.findByVerification(form.getCountry(), e.encrypt(form.getPersonalId())).orElseThrow(ResourceNotFoundException::new);
        return decrypt(customer);
    }

    public Customer findCustomerByVerification(LocalReservationForm form) throws RuntimeException {
        Customer customer = repository.findByVerification(form.getCountry(), e.encrypt(form.getPersonalId())).orElseThrow(ResourceNotFoundException::new);
        return decrypt(customer);
    }

    @Transactional
    public HttpStatus appendReservationToCustomer(Long customerId, ReservationForm form) {
        try {
            Customer customer = findById(customerId);
            return reservationService.createReservation(customer, form);
        } catch (ResourceNotFoundException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.NOT_FOUND;
        } catch (RuntimeException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Transactional
    public HttpStatus appendLocalReservationToCustomer(CustomUserDetails cud, LocalReservationForm form) {
        try {
            HttpStatus hasAccess = employeeService.departmentAccess(cud, form.getReservationForm().getIndexData().getDepartmentIdFrom());
            if (hasAccess.equals(HttpStatus.FORBIDDEN)) return HttpStatus.FORBIDDEN;

            Country country = countryService.findById(form.getCountry());
            Optional<Verification> verification = verificationService.getOptionalVerification(country, form.getPersonalId());
            if (verification.isEmpty()) {
                Customer customer = createGuest(form);
                verificationService.createVerification(customer.getId(), country, form.getPersonalId(), form.getDriverId());
                return reservationService.createReservation(customer, form.getReservationForm());
            } else {
                Customer customer = findById(verification.get().getId());
                HttpStatus status = reservationService.createReservation(customer, form.getReservationForm());
                if (status.equals(HttpStatus.CREATED)) return HttpStatus.OK;
                return status;
            }
        } catch (RuntimeException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Transactional
    public HttpStatus mergeCustomer(Long mainCustomerId, Long usedCustomerId) {
        try {
            Customer mainCustomer = findById(mainCustomerId);
            Customer usedCustomer = findById(usedCustomerId);

            verificationService.duplicateVerification(mainCustomerId, usedCustomerId);
            return deleteCustomer(usedCustomerId, reservationService.transferReservations(mainCustomer, usedCustomer));
        } catch (RuntimeException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public Map<Customer, Long> findCustomersWithResults(SearchCustomersForm form, boolean isArrival) throws RuntimeException {
        Reservation.ReservationStatus status = null;
        if (!form.getStatus().isEmpty()) {
            status = Reservation.ReservationStatus.valueOf(form.getStatus());
        }

        Map<Customer, Long> results;

        if (isArrival) {
            results = reservationService.findArrivalsByDetails(form, status).stream().collect(Collectors.groupingBy(Reservation::getCustomer, Collectors.counting()));
        } else {
            results = reservationService.findDeparturesByDetails(form, status).stream().collect(Collectors.groupingBy(Reservation::getCustomer, Collectors.counting()));
        }

        results.forEach((customer, aLong) -> decrypt(customer));
        return results;
    }

    @Transactional
    private Customer save(Customer customer) throws RuntimeException {
        return repository.save(encrypt(customer));
    }

    private Customer decrypt(Customer customer) throws RuntimeException {
        customer.setContactNumber(e.decrypt(customer.getContactNumber()));
        return customer;
    }

    private Customer encrypt(Customer customer) throws RuntimeException {
        customer.setContactNumber(e.encrypt(customer.getContactNumber()));
        return customer;
    }
}
