package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.property.payments.Currency;
import com.sda.carrental.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CurrencyService {
    private final CurrencyRepository repository;

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
}
