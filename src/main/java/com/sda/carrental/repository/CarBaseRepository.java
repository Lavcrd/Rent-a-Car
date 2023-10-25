package com.sda.carrental.repository;

import com.sda.carrental.model.property.car.CarBase;
import org.springframework.data.repository.CrudRepository;

public interface CarBaseRepository extends CrudRepository<CarBase, Long> {
}
