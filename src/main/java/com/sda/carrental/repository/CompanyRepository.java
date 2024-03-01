package com.sda.carrental.repository;

import com.sda.carrental.model.Company;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CompanyRepository extends CrudRepository<Company, Long> {

    @Query(value = "SELECT * FROM company WHERE id = 1", nativeQuery = true)
    Optional<Company> get();

    @Modifying
    @Query(value = "UPDATE company SET logotype = :reference WHERE id = 1", nativeQuery = true)
    void updateLogotypeReference(@Param("reference") String reference);

    @Modifying
    @Query(value = "UPDATE company SET name = :name, owner = :owner, website = :website  WHERE id = 1", nativeQuery = true)
    void updateDetails(@Param("name") String name, @Param("owner") String owner, @Param("website") String website);
}
