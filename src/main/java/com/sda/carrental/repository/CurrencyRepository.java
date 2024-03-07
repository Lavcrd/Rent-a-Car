package com.sda.carrental.repository;

import com.sda.carrental.model.property.payments.Currency;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CurrencyRepository extends CrudRepository<Currency, Long> {
    @Query("SELECT c FROM currency c ORDER BY c.code, c.name")
    List<Currency> findAll();
}
