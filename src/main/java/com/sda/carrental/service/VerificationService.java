package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.enums.Country;
import com.sda.carrental.model.users.auth.Verification;
import com.sda.carrental.repository.VerificationRepository;
import com.sda.carrental.web.mvc.form.VerificationForm;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VerificationService {
    private final VerificationRepository repository;

    @Transactional
    public HttpStatus deleteVerification(Long customerId) {
        try {
            repository.findByCustomerId(customerId).ifPresent(repository::delete);
            return HttpStatus.OK;
        } catch (RuntimeException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public Optional<Verification> getOptionalVerificationByCustomer(Long customerId) {
        return repository.findByCustomerId(customerId);
    }

    public Verification maskVerification(Verification verification) {
        return new Verification(verification.getCustomerId(), verification.getCountry(), verification.getPersonalId().replaceAll("^...", "XXX"), verification.getDriverId().replaceAll("...$", "XXX"));
    }

    @Transactional
    public HttpStatus createVerification(VerificationForm form) {
        try {
            if (getOptionalVerificationByCustomer(form.getCustomerId()).isEmpty()) {
                if (getOptionalVerification(form.getCountry(), form.getPersonalId()).isPresent())
                    return HttpStatus.CONFLICT;
                repository.save(new Verification(form.getCustomerId(), form.getCountry(), form.getPersonalId(), form.getDriverId()));
                return HttpStatus.CREATED;
            }
            return HttpStatus.EXPECTATION_FAILED;
        } catch (ResourceNotFoundException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.NOT_FOUND;
        } catch (DataIntegrityViolationException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.CONFLICT;
        }
    }

    @Transactional
    public void createVerification(Long customerId, Country country, String personalId, String driverId) {
        repository.save(new Verification(customerId, country, personalId, driverId));
    }

    public Optional<Verification> getOptionalVerification(Country country, String personalId) {
        return repository.findByVerificationFields(country, personalId);
    }
}
