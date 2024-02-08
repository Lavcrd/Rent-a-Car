package com.sda.carrental.repository;

import com.sda.carrental.global.enums.Country;
import com.sda.carrental.model.property.Department;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends CrudRepository<Department, Long> {
    List<Department> findDepartmentsByCountry(Country country);

    Optional<Department> findDepartmentByCountryAndHq(Country country, boolean hq);

    @Query("SELECT d FROM department d " +
            "WHERE d.isActive = :active " +
            "AND (:country IS NULL OR d.country = :country) " +
            "AND (:city IS NULL OR LOWER(d.city) LIKE LOWER(CONCAT(:city, '%'))) " +
            "AND (:address IS NULL OR LOWER(d.address) LIKE LOWER(CONCAT('%', :address, '%'))) " +
            "AND (:postcode IS NULL OR LOWER(d.postcode) LIKE LOWER(CONCAT(:postcode, '%'))) " +
            "AND (:hq = false OR d.hq = :hq) " +
            "ORDER BY d.country, d.postcode, d.city, d.address")
    List<Department> findAllByForm(@Param("city") String city, @Param("address") String address, @Param("postcode") String postcode,
                                   @Param("active") boolean active, @Param("hq") boolean hq, @Param("country") Country country);
}
