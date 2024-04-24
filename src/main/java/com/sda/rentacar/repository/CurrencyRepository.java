package com.sda.rentacar.repository;

import com.sda.rentacar.model.property.payments.Currency;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface CurrencyRepository extends CrudRepository<Currency, Long> {
    @Query("SELECT c FROM currency c ORDER BY c.code, c.name")
    List<Currency> findAll();

    @Query("SELECT c FROM currency c " +
            "WHERE (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:code IS NULL OR LOWER(c.code) LIKE LOWER(CONCAT('%', :code, '%'))) " +
            "ORDER BY c.code, c.name, c.exchange")
    List<Currency> findAllByForm(@Param("name") String name, @Param("code") String code);

    @Query("SELECT c FROM currency c " +
            "WHERE UPPER(:code) LIKE UPPER(c.code)")
    Optional<Currency> findByCode(@Param("code") String code);

    @Query(nativeQuery = true,
            value = "SELECT count(r1.id) FROM country r1 WHERE r1.currency = :id UNION ALL " +
                    "SELECT count(r2.id) FROM payment_details r2 WHERE r2.currency = :id")
    List<BigInteger> findUsageById(@Param("id") Long id);

    @Query(nativeQuery = true,
            value = "SELECT EXISTS (SELECT 1 FROM country c WHERE c.currency = :id) UNION ALL " +
                    "SELECT EXISTS (SELECT 1 FROM payment_details p WHERE p.currency = :id);")
    List<BigInteger> hasPresence(@Param("id") Long id);
}
