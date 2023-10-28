package com.sda.carrental.repository;

import com.sda.carrental.global.enums.Country;
import com.sda.carrental.model.property.car.CarBase;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CarBaseRepository extends CrudRepository<CarBase, Long> {
    @Query("SELECT cb " +
            "FROM car_base cb " +
            "WHERE cb.id IN (" +
            "  SELECT c.carBase.id " +
            "  FROM car c " +
            "  WHERE c.department.country = :country " +
            "  GROUP BY c.carBase.id " +
            "  HAVING COUNT(c.carBase.id) >= (" +
            "    SELECT COUNT(r.carBase.id) " +
            "    FROM reservation r " +
            "    WHERE r.carBase.id = c.carBase.id " +
            "    AND r.dateFrom BETWEEN :dateFrom AND :dateTo " +
            "    AND r.dateTo BETWEEN :dateFrom AND :dateTo" +
            "  )" +
            ")")
    List<CarBase> getAvailableCarBasesInCountry(@Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo, @Param("country") Country country);

    @Query("SELECT cb " +
            "FROM car_base cb " +
            "JOIN car c ON cb.id = c.carBase.id " +
            "WHERE c.department.id = :department " +
            "AND c.carStatus NOT IN (1, 2, 3) " +
            "GROUP BY cb.id")
    List<CarBase> getAvailableCarBasesInDepartment(@Param("department") Long department);

    @Query("SELECT cb " +
            "FROM car_base cb " +
            "WHERE cb.id = :id " +
            "AND EXISTS (" +
            "   SELECT c.carBase FROM car c " +
            "   WHERE c.department.id = :department " +
            "   AND c.carStatus NOT IN (1, 2, 3))")
    Optional<CarBase> getAvailableCarBaseInDepartment(@Param("id") Long id,@Param("department") Long department);
}
