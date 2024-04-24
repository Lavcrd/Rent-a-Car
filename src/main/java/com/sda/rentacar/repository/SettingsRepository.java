package com.sda.rentacar.repository;

import com.sda.rentacar.model.property.company.Settings;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SettingsRepository extends CrudRepository<Settings, Long> {

    @Query(value = "SELECT * FROM settings WHERE id = 1", nativeQuery = true)
    Optional<Settings> get();

    @Modifying
    @Query(value = "UPDATE settings " +
            "SET " +
            "refund_penalty_timeframe = :refundPenalty, " +
            "refund_management_timeframe  = :refundManagement, " +
            "cancellation_fee_percentage  = :cancellationPercentage, " +
            "reservation_gap = :reservationGap " +
            "WHERE id = 1", nativeQuery = true)
    void update(@Param("refundPenalty") long refundPenalty, @Param("refundManagement") long refundManagement,
                @Param("cancellationPercentage") double cancellationPercentage, @Param("reservationGap") long reservationGap);
}
