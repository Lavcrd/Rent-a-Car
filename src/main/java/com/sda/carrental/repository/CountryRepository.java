package com.sda.carrental.repository;

import com.sda.carrental.model.property.department.Country;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CountryRepository extends CrudRepository<Country, Long> {
        @Query("SELECT c FROM country c " +
            "WHERE c.isActive = :active " +
            "AND (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT(:name, '%'))) " +
            "AND (:code IS NULL OR LOWER(c.code) LIKE LOWER(CONCAT(:code, '%'))) " +
            "AND (:currency IS NULL OR LOWER(c.currency.code) LIKE LOWER(CONCAT(:currency, '%'))) " +
            "ORDER BY c.code, c.name, c.currency.code")
    List<Country> findAllByForm(@Param("name") String name, @Param("code") String code, @Param("currency") String currency, @Param("active") boolean active);
}
