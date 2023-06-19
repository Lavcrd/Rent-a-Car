package com.sda.carrental.repository;

import com.sda.carrental.global.enums.Country;
import com.sda.carrental.model.property.Department;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends CrudRepository<Department, Long> {
    List<Department> findDepartmentsByCountry(Country country);
    Optional<Department> findDepartmentByCountryAndHq(Country country, boolean hq);
}
