package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.users.Customer;
import com.sda.carrental.repository.CustomerRepository;
import com.sda.carrental.service.mappers.CustomerMapper;
import com.sda.carrental.web.mvc.form.ChangeAddressForm;
import com.sda.carrental.web.mvc.form.LocalReservationForm;
import com.sda.carrental.web.mvc.form.RegisterCustomerForm;
import com.sda.carrental.web.mvc.form.SearchCustomerForm;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository repository;
    private final CredentialsService credentialsService;

    public Customer findById(Long customerId) {
        return repository.findById(customerId).orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional(rollbackFor = {Exception.class})
    public HttpStatus createCustomer(RegisterCustomerForm form) {
        try {
            Customer customer = CustomerMapper.toEntity(form);
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

    @Transactional
    public HttpStatus changeAddress(ChangeAddressForm form, long customerId) {
        try {
            Customer user = findById(customerId);

            user.setCountry(form.getCountry());
            user.setCity(form.getCity());
            user.setAddress(form.getAddress());
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
        customer.setAddress("—");
        customer.setContactNumber("—");
        customer.setTerminationDate(LocalDate.now());
        customer.setStatus(Customer.CustomerStatus.STATUS_DELETED);

        return customer;
    }

    @Transactional(rollbackFor = {Exception.class})
    public HttpStatus deleteCustomer(Long customerId, boolean hasNoReservations) {
        try {
            Customer customer = findById(customerId);

            if (hasNoReservations) {
                repository.delete(customer);
            } else {
                repository.save(redactCustomer(customer));
            }
            return HttpStatus.OK;
        } catch (ResourceNotFoundException err) {
            return HttpStatus.NOT_FOUND;
        } catch (RuntimeException err) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public List<Customer> findCustomersByDepartmentAndName(SearchCustomerForm customersData) {
        if (customersData.getName().isEmpty()) customersData.setName(null);
        if (customersData.getSurname().isEmpty()) customersData.setSurname(null);
        return repository.findCustomersByDepartmentAndName(customersData.getDepartmentId(), customersData.getName(), customersData.getSurname());
    }

    @Transactional
    public Customer createGuest(LocalReservationForm form) {
        Customer customer = CustomerMapper.toGuestEntity(form);
        return repository.save(customer);
    }

    public Customer findCustomerByVerification(String personalId, String driverId) {
        return repository.findByVerification(personalId, driverId).orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional
    public void updateCustomerContactData(Long customerId, LocalReservationForm form) {
        try {
            Customer customer = repository.findById(customerId).orElseThrow(ResourceNotFoundException::new);
            if (customer.getStatus().equals(Customer.CustomerStatus.STATUS_REGISTERED)) return;
            customer.setCountry(form.getCountry());
            customer.setCity(form.getCity());
            customer.setAddress(form.getAddress());
            customer.setContactNumber(form.getContactNumber());
            repository.save(customer);
        } catch (ResourceNotFoundException | DataAccessException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }

    }
}
