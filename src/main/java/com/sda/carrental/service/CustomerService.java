package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.users.Customer;
import com.sda.carrental.repository.CustomerRepository;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.ChangeAddressForm;
import com.sda.carrental.web.mvc.form.SearchCustomerForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository repository;

    public Customer findByUsername(String username) {
        return repository.findByEmail(username).orElseThrow(() -> new RuntimeException("User with username: " + username + " not found"));
    }

    public HttpStatus changeContact(String inputContact) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Customer user = findByUsername(cud.getUsername());
            user.setContactNumber(inputContact);
            repository.save(user);
            return HttpStatus.ACCEPTED;
        } catch (ResourceNotFoundException err) {
            err.printStackTrace();
            return HttpStatus.NOT_FOUND;
        } catch (Error err) {
            err.printStackTrace();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public HttpStatus changeAddress(ChangeAddressForm form) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Customer user = findByUsername(cud.getUsername());

            user.setCountry(form.getCountry());
            user.setCity(form.getCity());
            user.setAddress(form.getAddress());
            repository.save(user);
            return HttpStatus.ACCEPTED;
        } catch (ResourceNotFoundException err) {
            err.printStackTrace();
            return HttpStatus.NOT_FOUND;
        } catch (Error err) {
            err.printStackTrace();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public String generateRandomString(int length) {
        final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-_=+`~";
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }

    private Customer scrambleCustomer(Customer customer) {
        customer.setPassword(generateRandomString(30));
        customer.setName("—");
        customer.setSurname("—");
        customer.setAddress("—");
        customer.setContactNumber("—");
        customer.setTerminationDate(LocalDate.now());

        String uniqueEmail;
        do {
            uniqueEmail = generateRandomString(30);
        } while (repository.findByEmail(uniqueEmail).isPresent());

        customer.setEmail(uniqueEmail);

        return customer;
    }

    @Transactional
    public HttpStatus deleteCustomer(Long customerId, boolean hasNoReservations) {
        try {
            Customer customer = findById(customerId);

            if (hasNoReservations) {
                repository.delete(customer);
            } else {
                repository.save(scrambleCustomer(customer));
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

    public Customer findById(Long customerId) {
        return repository.findById(customerId).orElseThrow(ResourceNotFoundException::new);
    }
}
