package com.sda.rentacar.repository;

import com.sda.rentacar.model.users.Admin;
import org.springframework.data.repository.CrudRepository;

public interface AdminRepository extends CrudRepository<Admin, Long> {

}
