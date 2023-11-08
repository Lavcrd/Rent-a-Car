package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.enums.Country;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.users.Customer;
import com.sda.carrental.model.users.auth.Verification;
import com.sda.carrental.repository.CustomerRepository;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.service.mappers.CustomerMapper;
import com.sda.carrental.web.mvc.form.operational.LocalReservationForm;
import com.sda.carrental.web.mvc.form.operational.SelectCarForm;
import com.sda.carrental.web.mvc.form.users.RegisterCustomerForm;
import com.sda.carrental.web.mvc.form.users.SearchCustomersForm;
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
    private final ReservationService reservationService;
    private final DepartmentService departmentService;

    public Customer findById(Long customerId) {
        return repository.findById(customerId).orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional(rollbackFor = {Exception.class})
    public HttpStatus createCustomer(RegisterCustomerForm form) {
        try {
            Customer customer = CustomerMapper.toRegisteredEntity(form);
            repository.save(customer);
            credentialsService.createCredentials(customer.getId(), form.getUsername(), form.getPassword());
            return HttpStatus.CREATED;
        } catch (Exception err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Transactional
    public HttpStatus changeContact(String inputContact, long customerId) {
        try {
            Customer user = findById(customerId);
            user.setContactNumber(inputContact);
            repository.save(user);
            return HttpStatus.ACCEPTED;
        } catch (ResourceNotFoundException err) {
            return HttpStatus.NOT_FOUND;
        } catch (Error err) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    private Customer redactCustomer(Customer customer) {
        customer.setName("—");
        customer.setSurname("—");
        customer.setContactNumber("—");
        customer.setTerminationDate(LocalDate.now());
        customer.setStatus(Customer.CustomerStatus.STATUS_DELETED);

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
        } catch (ResourceNotFoundException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.NOT_FOUND;
        } catch (RuntimeException err) {
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
    public Customer createGuest(LocalReservationForm form) {
        Customer customer = CustomerMapper.toGuestEntity(form);
        return repository.save(customer);
    }

    public Customer findCustomerByVerification(Country country, String personalId) throws ResourceNotFoundException {
        return repository.findByVerification(country, personalId).orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional
    public HttpStatus appendReservationToCustomer(Long customerId, SelectCarForm form) {
        try {
            Customer customer = findById(customerId);
            return reservationService.createReservation(customer, form);
        } catch (ResourceNotFoundException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.NOT_FOUND;
        }
    }

    @Transactional
    public HttpStatus appendLocalReservationToCustomer(CustomUserDetails cud, LocalReservationForm form) {
        try {
            HttpStatus hasAccess = departmentService.departmentAccess(cud, form.getReservationForm().getIndexData().getDepartmentIdFrom());
            if (hasAccess.equals(HttpStatus.FORBIDDEN)) return HttpStatus.FORBIDDEN;

            Country country = Country.valueOf(form.getCountry());
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

        } catch (RuntimeException err) {
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
        } catch (RuntimeException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public Map<Customer, Long> findCustomersWithResults(SearchCustomersForm form, boolean isArrival) {
        if (isArrival) {
            return reservationService.findArrivalsByDetails(form).stream().collect(Collectors.groupingBy(Reservation::getCustomer, Collectors.counting()));
        } else {
            return reservationService.findDeparturesByDetails(form).stream().collect(Collectors.groupingBy(Reservation::getCustomer, Collectors.counting()));
        }
    }
}
