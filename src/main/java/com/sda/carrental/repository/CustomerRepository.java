package com.sda.carrental.repository;

import com.sda.carrental.model.users.Customer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerRepository extends CrudRepository<Customer, Long> {
    @Query(value = "SELECT c FROM verification v LEFT JOIN customer c ON c.id = v.id " +
            "WHERE v.personalId = :personalId AND v.country.id = :countryId")
    Optional<Customer> findByVerification(@Param("countryId") Long countryId, @Param("personalId") String personalId);
}
