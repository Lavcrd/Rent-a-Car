package com.sda.carrental.service;

import com.sda.carrental.model.operational.Renting;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.property.Car;
import com.sda.carrental.model.users.User;
import com.sda.carrental.repository.RentingRepository;
import com.sda.carrental.service.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentingService {
    private final RentingRepository repository;
    private final UserService userService;
    private final CarService carService;

    @Transactional
    public void createRent(Reservation reservation) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User employee = userService.findByUsername(cud.getUsername());
        carService.updateCarStatus(reservation.getCar(), Car.CarStatus.STATUS_RENTED);
        repository.save(new Renting(employee, reservation));
    }
}
