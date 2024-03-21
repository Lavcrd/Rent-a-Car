package com.sda.carrental.service;

import com.sda.carrental.exceptions.IllegalActionException;
import com.sda.carrental.global.Encryption;
import com.sda.carrental.model.property.department.Country;
import com.sda.carrental.model.users.auth.Verification;
import com.sda.carrental.repository.VerificationRepository;
import com.sda.carrental.web.mvc.form.users.customer.VerificationForm;
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
    private final CountryService countryService;
    private final Encryption e;

    @Transactional
    public HttpStatus deleteVerification(Long customerId) {
        try {
            repository.findById(customerId).ifPresent(repository::delete);
            return HttpStatus.OK;
        } catch (RuntimeException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public Optional<Verification> getOptionalVerificationByCustomer(Long customerId) throws RuntimeException {
        Optional<Verification> ov = repository.findById(customerId);
        return ov.map(this::decrypt);
    }

    public Verification maskVerification(Verification verification) {
        return new Verification(verification.getId(), verification.getCountry(), verification.getPersonalId().replaceAll("^...", "XXX"), verification.getDriverId().replaceAll("...$", "XXX"));
    }

    @Transactional
    public HttpStatus appendVerification(Long customerId, VerificationForm form) {
        try {
            if (getOptionalVerificationByCustomer(customerId).isEmpty()) {
                Country country = countryService.findById(form.getCountry());
                if (getOptionalVerification(country, form.getPersonalId()).isPresent())
                    return HttpStatus.CONFLICT;
                save(new Verification(customerId, country, form.getPersonalId(), form.getDriverId()));
                return HttpStatus.CREATED;
            }
            return HttpStatus.EXPECTATION_FAILED;
        } catch (DataIntegrityViolationException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.CONFLICT;
        } catch (RuntimeException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.BAD_REQUEST;
        }
    }

    @Transactional
    public void createVerification(Long customerId, Country country, String personalId, String driverId) throws RuntimeException {
        save(new Verification(customerId, country, personalId, driverId));
    }

    public Optional<Verification> getOptionalVerification(Country country, String personalId) throws RuntimeException {
       Optional<Verification> ov = repository.findByVerificationFields(country, e.encrypt(personalId));
        return ov.map(this::decrypt);
    }

    @Transactional
    public void duplicateVerification(Long mainCustomerId, Long usedCustomerId) throws RuntimeException {
        Optional<Verification> verification = repository.findById(usedCustomerId);
        if (verification.isEmpty()) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new IllegalActionException();
        }

        if (!repository.existsById(mainCustomerId)) {
            Verification v = decrypt(verification.get());
            save(new Verification(mainCustomerId, v.getCountry(), v.getPersonalId(), v.getDriverId()));
        } else {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new IllegalActionException();
        }
    }

    @Transactional
    private void save(Verification verification) throws RuntimeException {
        repository.save(encrypt(verification));
    }

    private Verification encrypt(Verification verification) throws RuntimeException {
        verification.setPersonalId(e.encrypt(verification.getPersonalId()));
        verification.setDriverId(e.encrypt(verification.getDriverId()));
        return verification;
    }

    private Verification decrypt(Verification verification) throws RuntimeException {
        verification.setPersonalId(e.decrypt(verification.getPersonalId()));
        verification.setDriverId(e.decrypt(verification.getDriverId()));
        return verification;
    }
}
