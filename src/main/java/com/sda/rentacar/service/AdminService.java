package com.sda.rentacar.service;

import com.sda.rentacar.exceptions.ResourceNotFoundException;
import com.sda.rentacar.global.Encryption;
import com.sda.rentacar.global.enums.Role;
import com.sda.rentacar.model.users.Admin;
import com.sda.rentacar.repository.AdminRepository;
import com.sda.rentacar.service.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepository repository;
    private final EntityManager entityManager;
    private final Encryption e;

    public Admin findById(Long id) throws RuntimeException {
        Admin admin = repository.findById(id).orElseThrow(ResourceNotFoundException::new);
        entityManager.detach(admin);
        return decrypt(admin);
    }

    public boolean hasAdminAuthority(CustomUserDetails cud) {
        try {
            return findById(cud.getId()).getRole().equals(Role.ROLE_ADMIN);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private Admin decrypt(Admin admin) throws RuntimeException {
        admin.setContactNumber(e.decrypt(admin.getContactNumber()));
        return admin;
    }
}
