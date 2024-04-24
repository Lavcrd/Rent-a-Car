package com.sda.rentacar.repository;

import com.sda.rentacar.model.property.department.Country;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;

public interface CountryRepository extends CrudRepository<Country, Long> {
        @Query("SELECT c FROM country c " +
            "WHERE c.isActive = :active " +
            "AND (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT(:name, '%'))) " +
            "AND (:code IS NULL OR UPPER(c.code) LIKE UPPER(CONCAT(:code, '%'))) " +
            "AND (:currency IS NULL OR LOWER(c.currency.code) LIKE LOWER(CONCAT(:currency, '%'))) " +
            "ORDER BY c.code, c.name, c.currency.code")
    List<Country> findAllByForm(@Param("name") String name, @Param("code") String code, @Param("currency") String currency, @Param("active") boolean active);

    @Query(nativeQuery = true,
            value = "SELECT EXISTS (SELECT 1 FROM department d WHERE d.country = :id) UNION ALL " +
                    "SELECT EXISTS (SELECT 1 FROM verification v WHERE v.country = :id);")
    List<BigInteger> hasPresence(@Param("id") Long id);
}
