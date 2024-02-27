package com.sda.carrental.repository;

import com.sda.carrental.model.Company;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CompanyRepository extends CrudRepository<Company, Long> {

    @Query(value = "SELECT * FROM company WHERE id = 1", nativeQuery = true)
    Optional<Company> get();
}
