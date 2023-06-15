package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.users.Customer;
import com.sda.carrental.model.users.Verification;
import com.sda.carrental.repository.VerificationRepository;
import com.sda.carrental.web.mvc.form.VerificationForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VerificationService {
    private final VerificationRepository repository;
    private final CustomerService customerService;

    public HttpStatus verificationDelete(Long customerId) {
        try {
            repository.delete(repository.findByCustomer(customerService.findById(customerId)).orElseThrow(ResourceNotFoundException::new));
            return HttpStatus.OK;
        } catch (ResourceNotFoundException err) {
            return HttpStatus.OK;
        } catch (RuntimeException err) {
            err.printStackTrace();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public Optional<Verification> getOptionalVerificationByCustomer(Customer customer) {
        return repository.findByCustomer(customer);
    }

    public Verification maskVerification(Verification verification) {
        return new Verification(verification.getCustomer(), verification.getPersonalId().replaceAll("^...", "XXX"), verification.getDriverId().replaceAll("...$", "XXX"));
    }

    public HttpStatus createVerification(VerificationForm form) {
        try {
            Customer customer = customerService.findById(form.getCustomerId());

            if (getOptionalVerificationByCustomer(customer).isEmpty()) {
                repository.save(new Verification(customer, form.getPersonalId(), form.getDriverId()));
                return HttpStatus.CREATED;
            }
            return HttpStatus.EXPECTATION_FAILED;
        } catch (ResourceNotFoundException err) {
            return HttpStatus.NOT_FOUND;
        }
    }
}
