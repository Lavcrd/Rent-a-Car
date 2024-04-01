package com.sda.carrental.repository;

import com.sda.carrental.model.property.department.Country;
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
    List<CarBase> findAvailableCarBasesInCountry(@Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo, @Param("country") Country country);

    @Query("SELECT cb " +
            "FROM car_base cb " +
            "JOIN car c ON cb.id = c.carBase.id " +
            "WHERE c.department.id = :department " +
            "AND c.carStatus NOT IN (1, 2, 3) " +
            "GROUP BY cb.id")
    List<CarBase> findAvailableCarBasesInDepartment(@Param("department") Long department);

    @Query("SELECT cb " +
            "FROM car_base cb " +
            "WHERE cb.id = :id " +
            "AND EXISTS (" +
            "   SELECT c.carBase FROM car c " +
            "   WHERE c.department.id = :department " +
            "   AND c.carStatus NOT IN (1, 2, 3))")
    Optional<CarBase> findAvailableCarBaseInDepartment(@Param("id") Long id, @Param("department") Long department);

    @Query("SELECT cb " +
            "FROM car_base cb " +
            "WHERE (:depositMin IS NULL OR cb.depositValue >= :depositMin) " +
            "AND (:depositMax IS NULL OR cb.depositValue <= :depositMax) " +
            "AND (COALESCE(:years, 0) = 0 OR cb.year IN (:years))")
    List<CarBase> findByDepositAndModelYear(@Param("depositMin") Double depositMin, @Param("depositMax") Double depositMax, @Param("years") List<Integer> years);

    @Query("SELECT cb " +
            "FROM car_base cb " +
            "WHERE cb.id = :id " +
            "AND NOT EXISTS (" +
            "   SELECT 1 " +
            "   FROM car c " +
            "   WHERE c.carBase.id = cb.id)")
    Optional<CarBase> findByIdAndNoCars(@Param("id") Long id);

    @Query("SELECT cb " +
            "FROM car_base cb " +
            "ORDER BY cb.brand ASC, cb.model ASC, " +
            "cb.year ASC, cb.seats ASC, " +
            "cb.priceDay ASC, cb.depositValue ASC")
    List<CarBase> findAllAndSort();


    @Query(value =
            "SELECT " +
                    "    CONCAT(cb.brand, ' ', cb.model, ' (', cb.year, ')') AS car, " +
                    "    SUM(CASE WHEN c.department = :department AND c.status = 0 THEN 1 ELSE 0 END) AS open, " +
                    "    SUM(CASE WHEN c.department = :department AND c.status = 1 THEN 1 ELSE 0 END) AS rent, " +
                    "    SUM(CASE WHEN c.department = :department AND c.status = 2 THEN 1 ELSE 0 END) AS unavailable, " +
                    "    (SELECT COUNT(r.id) FROM reservation r WHERE r.department_id = :department AND r.car_base = cb.id AND r.status IN (2,3) AND r.date_from <= :nextWeekEnd) AS departureWeek, " +
                    "    (SELECT COUNT(r.id) FROM reservation r WHERE r.department_id = :department AND r.car_base = cb.id AND r.status IN (2,3)) AS departureAll, " +
                    "    (SELECT COUNT(r.id) FROM rent re JOIN reservation r ON re.id = r.id WHERE r.department_return_id = :department AND r.car_base = cb.id AND r.status = 1 AND r.date_to <= :nextWeekEnd) AS arrivalWeek, " +
                    "    (SELECT COUNT(r.id) FROM rent re JOIN reservation r ON re.id = r.id WHERE r.department_return_id = :department AND r.car_base = cb.id AND r.status = 1) AS arrivalAll " +
                    "FROM " +
                    "    car_base cb " +
                    "LEFT JOIN " +
                    "    car c ON cb.id = c.car_base " +
                    "GROUP BY " +
                    "    cb.id " +
                    "HAVING " +
                    "   (open > 0 OR rent > 0 OR unavailable > 0 OR departureAll > 0 OR departureWeek > 0 OR arrivalWeek > 0 OR arrivalAll > 0) " +
                    "ORDER BY " +
                    "    cb.brand, cb.model;",
            nativeQuery = true)
    List<Object[]> getCarBaseStatisticsByDepartment(@Param("department") long department, @Param("nextWeekEnd") LocalDate nextWeekEnd);
}
