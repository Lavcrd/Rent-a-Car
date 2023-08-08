package com.sda.carrental.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.sda.carrental.model.property.Car;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;


public interface CarRepository extends CrudRepository<Car, Long> {

    @Query(value = "SELECT c2 FROM car c2 WHERE id IN (" +
            "SELECT c.id " +
            "FROM car c " +
            "LEFT JOIN reservation r ON c.id = r.car.id " +
            "WHERE c.departmentId = :department " +
            "   AND c.carStatus <> 3 " +
            "   AND (" +
            "       r.reservationId IS NULL " +
            "       OR r.car.id NOT IN (" +
            "       SELECT r2.car.id " +
            "       FROM reservation r2 " +
            "       WHERE r2.dateFrom <= :dateTo AND r2.dateTo >= :dateFrom " +
            "       AND r2.status IN (1, 3) " +
            "       GROUP BY r2.car.id " +
            "       HAVING COUNT(*) >= 1)) " +
            "GROUP BY c.id)" +
            "GROUP BY c2.model, c2.brand")
    List<Car> findAvailableDistinctCarsInDepartment(@Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo, @Param("department") Long department);

    @Query(value = "SELECT c1 FROM car c1 WHERE id IN (" +
            "SELECT c.id " +
            "FROM car c " +
            "LEFT JOIN reservation r ON c.id = r.car.id " +
            "WHERE c.departmentId = :department " +
            "   AND c.carStatus <> 3 " +
            "   AND (" +
            "       r.reservationId IS NULL " +
            "       OR r.car.id NOT IN (" +
            "       SELECT r2.car.id " +
            "       FROM reservation r2 " +
            "       WHERE r2.dateFrom <= :dateTo AND r2.dateTo >= :dateFrom " +
            "       AND r2.status IN (1, 3) " +
            "       GROUP BY r2.car.id " +
            "       HAVING COUNT(*) >= 1)) " +
            "GROUP BY c.id)")
    List<Car> findAvailableCarsInDepartment(@Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo, @Param("department") Long department);

    @Query(value = "SELECT c1 FROM car c1 WHERE id = :carId " +
            "AND id IN (" +
            "SELECT c.id " +
            "FROM car c " +
            "LEFT JOIN reservation r ON c.id = r.car.id " +
            "WHERE c.departmentId = :department " +
            "   AND c.carStatus <> 3 " +
            "   AND (" +
            "       r.reservationId IS NULL " +
            "       OR r.car.id NOT IN (" +
            "       SELECT r2.car.id " +
            "       FROM reservation r2 " +
            "       WHERE r2.dateFrom <= :dateTo AND r2.dateTo >= :dateFrom " +
            "       AND r2.status IN (1, 3) " +
            "       GROUP BY r2.car.id " +
            "       HAVING COUNT(*) >= 1)) " +
            "GROUP BY c.id)")
    Optional<Car> findCarByCarIdAndAvailability(@Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo, @Param("department") Long department, @Param("carId") long carId);
}
