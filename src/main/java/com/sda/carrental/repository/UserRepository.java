package com.sda.carrental.repository;

import com.sda.carrental.model.users.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
}
