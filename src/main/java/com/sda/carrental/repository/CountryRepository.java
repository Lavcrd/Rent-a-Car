package com.sda.carrental.repository;

import com.sda.carrental.model.operational.Country;
import org.springframework.data.repository.CrudRepository;

public interface CountryRepository extends CrudRepository<Country, Long> {
}
