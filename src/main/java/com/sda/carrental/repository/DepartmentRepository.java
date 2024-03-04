package com.sda.carrental.repository;

import com.sda.carrental.model.property.department.Country;
import com.sda.carrental.model.property.department.Department;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends CrudRepository<Department, Long> {
    List<Department> findDepartmentsByCountry(Country country);

    Optional<Department> findDepartmentByCountryAndHq(Country country, boolean hq);

    @Query("SELECT d FROM department d " +
            "WHERE d.isActive = :active " +
            "AND (:country IS NULL OR LOWER(d.country.name) LIKE LOWER(CONCAT(:country, '%'))) " +
            "AND (:city IS NULL OR LOWER(d.city) LIKE LOWER(CONCAT(:city, '%'))) " +
            "AND (:street IS NULL OR LOWER(d.street) LIKE LOWER(CONCAT('%', :street, '%'))) " +
            "AND (:building IS NULL OR LOWER(d.building) LIKE LOWER(CONCAT('%', :building, '%'))) " +
            "AND (:postcode IS NULL OR LOWER(d.postcode) LIKE LOWER(CONCAT(:postcode, '%'))) " +
            "AND (:hq = false OR d.hq = :hq) " +
            "ORDER BY d.country, d.postcode, d.city, d.street, d.building")
    List<Department> findAllByForm(@Param("city") String city, @Param("street") String street, @Param("building") String building, @Param("postcode") String postcode,
                                   @Param("active") boolean active, @Param("hq") boolean hq, @Param("country") String country);

    @Query(nativeQuery = true,
            value = "SELECT EXISTS (SELECT 1 FROM reservation r WHERE r.department_id = :id) UNION ALL " +
                    "SELECT EXISTS (SELECT 1 FROM reservation r WHERE r.department_return_id = :id) UNION ALL " +
                    "SELECT EXISTS (SELECT 1 FROM retrieve r WHERE r.department_id = :id) UNION ALL " +
                    "SELECT EXISTS (SELECT 1 FROM car c WHERE c.department = :id) UNION ALL " +
                    "SELECT EXISTS (SELECT 1 FROM employee_departments ed WHERE ed.departments_id = :id);")
    List<BigInteger> hasPresence(@Param("id") Long id);
}
