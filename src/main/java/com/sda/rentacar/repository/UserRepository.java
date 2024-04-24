package com.sda.rentacar.repository;

import com.sda.rentacar.model.users.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
}
