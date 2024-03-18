package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.Utility;
import com.sda.carrental.model.property.department.Country;
import com.sda.carrental.model.property.payments.Currency;
import com.sda.carrental.repository.CountryRepository;
import com.sda.carrental.web.mvc.form.property.departments.country.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class CountryService {
    private final CountryRepository repository;
    private final CurrencyService currencyService;
    private final Utility u;

    public Country findById(Long id) throws ResourceNotFoundException {
        return repository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    public List<Country> findAll() {
        return StreamSupport
                .stream(repository.findAll().spliterator(), false)
                .collect(toList());
    }

    public Country placeholder() {
        return new Country("N/D", "N/D", "N/D", currencyService.placeholder());
    }

    public List<Country> findByForm(SearchCountriesForm form) {
        return repository.findAllByForm(form.getName(), form.getCode().toUpperCase(), form.getCurrency(), form.isActive());
    }

    public List<Country> findByForm(RegisterCountryForm form, String currency) {
        return repository.findAllByForm(form.getName(), form.getCode().toUpperCase(), currency, true);
    }

    @Transactional
    public HttpStatus register(RegisterCountryForm form) {
        try {
            Currency currency = currencyService.findById(form.getCurrency());
            if (!findByForm(form, currency.getCode()).isEmpty()) return HttpStatus.CONFLICT;

            Country country = new Country(form.getName(), form.getCode().toUpperCase(), form.getContact(), currency);
            repository.save(country);
            return HttpStatus.CREATED;
        } catch (RuntimeException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public boolean hasPresence(Long id) {
        List<BigInteger> results = repository.hasPresence(id);
        for (BigInteger result : results) {
            if (result.intValue() == 1) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    public HttpStatus updateDetails(Long id, UpdateCountryDetailsForm form) {
        try {
            Country country = findById(id);

            country.setName(form.getName());
            country.setCode(form.getCode().toUpperCase());
            country.setContact(form.getContact());

            repository.save(country);
            return HttpStatus.ACCEPTED;
        } catch (RuntimeException e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Transactional
    public HttpStatus updateCurrency(Long id, UpdateCountryCurrencyForm form) {
        try {
            Country country = findById(id);
            Currency currency = currencyService.findById(form.getCurrency());

            country.setCurrency(currency);

            repository.save(country);
            return HttpStatus.ACCEPTED;
        } catch (RuntimeException e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Transactional
    public HttpStatus updateRelocationFee(Long id, UpdateRelocationFeeForm form) {
        try {
            Country country = findById(id);
            Double value = u.roundCurrency(Double.parseDouble(form.getFee()));

            country.setRelocateCarPrice(value);

            repository.save(country);
            return HttpStatus.ACCEPTED;
        } catch (RuntimeException e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Transactional
    public HttpStatus updateActivity(Long id) {
        try {
            Country country = findById(id);
            country.setActive(!country.isActive());

            repository.save(country);
            return HttpStatus.ACCEPTED;
        } catch (RuntimeException e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Transactional
    public HttpStatus delete(Long id) {
        try {
            if (hasPresence(id)) {
                return HttpStatus.PRECONDITION_FAILED;
            }
            repository.deleteById(id);
            return HttpStatus.ACCEPTED;
        } catch (RuntimeException e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
