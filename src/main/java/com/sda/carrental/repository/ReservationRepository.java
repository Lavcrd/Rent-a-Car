package com.sda.carrental.repository;

import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.users.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends CrudRepository<Reservation, Long> {
    List<Reservation> findAll();

    @Query(value = "from reservation where customer = :user order by reservationId desc")
    List<Reservation> findAllByUser(@Param("user") User user);

    @Query(value = "from reservation where customer = :user and reservationId = :id")
    Optional<Reservation> findByUserAndId(@Param("user") User user, @Param("id") Long id);
}
