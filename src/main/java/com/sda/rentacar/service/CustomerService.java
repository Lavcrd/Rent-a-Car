package com.sda.rentacar.service;

import com.sda.rentacar.exceptions.ResourceNotFoundException;
import com.sda.rentacar.global.Encryption;
import com.sda.rentacar.global.Utility;
import com.sda.rentacar.model.property.department.Country;
import com.sda.rentacar.model.operational.Reservation;
import com.sda.rentacar.model.users.Customer;
import com.sda.rentacar.model.users.auth.Verification;
import com.sda.rentacar.repository.CustomerRepository;
import com.sda.rentacar.service.auth.CustomUserDetails;
import com.sda.rentacar.service.mappers.CustomerMapper;
import com.sda.rentacar.web.mvc.form.operational.LocalReservationForm;
import com.sda.rentacar.web.mvc.form.operational.ReservationForm;
import com.sda.rentacar.web.mvc.form.users.customer.FindVerifiedForm;
import com.sda.rentacar.web.mvc.form.users.customer.RegisterCustomerForm;
import com.sda.rentacar.web.mvc.form.users.customer.SearchCustomersForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final EntityManager entityManager;
    private final CustomerRepository repository;
    private final CredentialsService credentialsService;
    private final VerificationService verificationService;
    private final CountryService countryService;
    private final ReservationService reservationService;
    private final EmployeeService employeeService;
    private final Encryption e;
    private final Utility u;

    public Customer findById(Long customerId) throws RuntimeException {
        Customer customer = repository.findById(customerId).orElseThrow(ResourceNotFoundException::new);
        entityManager.detach(customer);
        return decrypt(customer);
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
        Customer result = save(customer);
        return decrypt(result);
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

                if (status.equals(HttpStatus.CREATED)) {
                    return HttpStatus.OK;
                }
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

    public Map<Customer, Long> findCustomersWithReservationCount(SearchCustomersForm form, boolean isArrival) throws RuntimeException {
        Reservation.ReservationStatus status = null;
        if (!form.getStatus().isEmpty()) {
            status = Reservation.ReservationStatus.valueOf(form.getStatus());
        }

        List<Object[]> results;

        if (isArrival) {
            results = repository.findCustomersArrivalCounts(
                    form.getCustomerName(), form.getCustomerSurname(),
                    form.getPrimaryDepartment(), form.getSecondaryDepartment(),
                    form.getDateFrom(), form.getDateTo(),
                    status
            );
        } else {
            results = repository.findCustomersDepartureCounts(
                    form.getCustomerName(), form.getCustomerSurname(),
                    form.getPrimaryDepartment(), form.getSecondaryDepartment(),
                    form.getDateFrom(), form.getDateTo(),
                    status
            );
        }

        Map<Customer, Long> map = new LinkedHashMap<>();
        for (Object[] result : results) {
            Customer customer = (Customer) result[0];
            Long count = (Long) result[1];
            map.put(customer, count);
        }

        map.forEach((customer, aLong) -> decrypt(customer));
        return map;
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
