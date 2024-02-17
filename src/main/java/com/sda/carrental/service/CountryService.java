package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.operational.Country;
import com.sda.carrental.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
