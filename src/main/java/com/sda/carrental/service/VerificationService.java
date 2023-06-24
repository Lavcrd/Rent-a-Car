package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.users.auth.Verification;
import com.sda.carrental.repository.VerificationRepository;
import com.sda.carrental.web.mvc.form.VerificationForm;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VerificationService {
    private final VerificationRepository repository;

    @Transactional
    public HttpStatus deleteVerification(Long customerId) {
        try {
            Optional<Verification> verification = repository.findByCustomerId(customerId);
            verification.ifPresent(repository::delete);
            return HttpStatus.OK;
        } catch (RuntimeException err) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public Optional<Verification> getOptionalVerificationByCustomer(Long customerId) {
        return repository.findByCustomerId(customerId);
    }

    public Verification maskVerification(Verification verification) {
        return new Verification(verification.getCustomerId(), verification.getPersonalId().replaceAll("^...", "XXX"), verification.getDriverId().replaceAll("...$", "XXX"));
    }

    public HttpStatus createVerification(VerificationForm form) {
        try {
            if (getOptionalVerificationByCustomer(form.getCustomerId()).isEmpty()) {
                repository.save(new Verification(form.getCustomerId(), form.getPersonalId(), form.getDriverId()));
                return HttpStatus.CREATED;
            }
            return HttpStatus.EXPECTATION_FAILED;
        } catch (ResourceNotFoundException err) {
            return HttpStatus.NOT_FOUND;
        } catch (DataIntegrityViolationException err) {
            return HttpStatus.CONFLICT;
        }
    }
}
