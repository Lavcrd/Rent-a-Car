package com.sda.carrental.repository;

import com.sda.carrental.model.operational.Rent;
import org.springframework.data.repository.CrudRepository;

public interface RentRepository extends CrudRepository<Rent, Long> {
}
