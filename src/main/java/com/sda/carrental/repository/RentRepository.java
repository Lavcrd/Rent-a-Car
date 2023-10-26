package com.sda.carrental.repository;

import com.sda.carrental.model.operational.Rent;
import com.sda.carrental.model.property.car.Car;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RentRepository extends CrudRepository<Rent, Long> {
    @Query("SELECT r FROM rent r " +
            "WHERE (r.reservation.car = :car) " +
            "AND (CURRENT_DATE BETWEEN r.dateFrom AND r.reservation.dateTo) " +
            "AND r.id NOT IN (" +
            "   SELECT r1 FROM retrieve r1 " +
            "   WHERE r1.id = r.id)")
    Optional<Rent> findActiveByCar(@Param("car") Car car);

    @Query(value = "SELECT r FROM rent r " +
            "WHERE r.car.plate = :plate " +
            "AND r.reservation.status = 1")
    Optional<Rent> findActiveOperationByCarPlate(@Param("plate") String plate);
}
