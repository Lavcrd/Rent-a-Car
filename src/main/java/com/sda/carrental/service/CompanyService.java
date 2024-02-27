package com.sda.carrental.service;

import com.sda.carrental.model.Company;
import com.sda.carrental.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository repository;


    public Company get() {
        return repository.get().orElse(new Company("—", "—", "—", "—"));
    }
}