package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.property.department.Country;
import com.sda.carrental.repository.CountryRepository;
import com.sda.carrental.web.mvc.form.property.departments.country.RegisterCountryForm;
import com.sda.carrental.web.mvc.form.property.departments.country.SearchCountriesForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class CountryService {
    private final CountryRepository repository;

    public Country findById(Long id) throws ResourceNotFoundException {
        return repository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    public List<Country> findAll() {
        return StreamSupport
                .stream(repository.findAll().spliterator(), false)
                .collect(toList());
    }

    public Country placeholder() {
        return new Country("N/D", "N/D", "N/D", "EUR", 1.0);
    }

    public List<Country> findByForm(SearchCountriesForm form) {
        return repository.findAllByForm(form.getName(), form.getCode(), form.getCurrency(), form.isActive());
    }

    public List<Country> findByForm(RegisterCountryForm form) {
        return repository.findAllByForm(form.getName(), form.getCode(), form.getCurrency(), true);
    }

    @Transactional
    public HttpStatus register(RegisterCountryForm form) {
        try {
            if (!findByForm(form).isEmpty()) return HttpStatus.CONFLICT;

            Country country = new Country(form.getName(), form.getCode().toUpperCase(), form.getContact(), form.getCurrency().toUpperCase(), Double.parseDouble(form.getExchange()));
            repository.save(country);
            return HttpStatus.CREATED;
        } catch (RuntimeException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
