package com.sda.rentacar.service;

import com.sda.rentacar.exceptions.ResourceNotFoundException;
import com.sda.rentacar.global.Utility;
import com.sda.rentacar.model.property.payments.Currency;
import com.sda.rentacar.repository.CurrencyRepository;
import com.sda.rentacar.web.mvc.form.property.payments.RegisterCurrencyForm;
import com.sda.rentacar.web.mvc.form.property.payments.SearchCurrenciesForm;
import com.sda.rentacar.web.mvc.form.property.payments.UpdateCurrencyDetailsForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigInteger;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CurrencyService {
    private final CurrencyRepository repository;
    private final Utility u;
    private static final long updateFrequency = 6 * 60 * 60 * 1000;

    public List<Currency> findAll() {
        return repository.findAll();
    }

    public Currency findById(Long id) throws ResourceNotFoundException {
        return repository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    // Default currency
    public Currency placeholder() {
        return new Currency("Euro", "EUR", 1.0);
    }

    public List<Currency> findByForm(SearchCurrenciesForm form) {
        return repository.findAllByForm(form.getName(), form.getCode());
    }

    public Optional<Currency> findByCode(String code) {
        return repository.findByCode(code);
    }

    @Transactional
    public HttpStatus register(RegisterCurrencyForm form) {
        try {
            if (findByCode(form.getCode()).isPresent()) return HttpStatus.CONFLICT;

            Currency currency = new Currency(form.getName(), form.getCode().toUpperCase(), u.roundCurrency(Double.parseDouble(form.getExchange())));
            repository.save(currency);
            return HttpStatus.CREATED;
        } catch (RuntimeException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public Integer[] findUsageById(Long id) {
        List<BigInteger> results = repository.findUsageById(id);
        Integer[] arr = new Integer[results.size()];
        for (int i = 0; i < results.size(); i++) {
            arr[i] = results.get(i).intValue();
        }
        return arr;
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
    public HttpStatus updateDetails(Long id, UpdateCurrencyDetailsForm form) {
        try {
            Currency currency = findById(id);
            if (findByCode(form.getCode()).isPresent()) return HttpStatus.CONFLICT;

            currency.setName(form.getName());
            currency.setCode(form.getCode().toUpperCase());

            repository.save(currency);
            return HttpStatus.ACCEPTED;
        } catch (RuntimeException e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Transactional
    public HttpStatus updateExchange(Long id) {
        try {
            Currency currency = findById(id);
            String defaultCurrency = placeholder().getCode();

            // Idea would be to use the last fetched JSON of currency exchange rates and calculate rate from defaultCurrency ISO and currency.getCode() ISO results
            Double result = 1D;

            currency.setExchange(u.roundCurrency(result));
            repository.save(currency);
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

    @Scheduled(fixedRate = updateFrequency)
    public void updateExchangeRates() {
        List<Currency> currencies = repository.findAll();
        Map<String, Double> json = new HashMap<>(); // Should be method that returns currency JSON
        /*for (Currency currency:currencies) {
            currency.setExchange(u.roundCurrency(json.get(currency.getCode())));
        }*/
        repository.saveAll(currencies);
    }
}
