package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.Encryption;
import com.sda.carrental.global.enums.Role;
import com.sda.carrental.model.users.Admin;
import com.sda.carrental.repository.AdminRepository;
import com.sda.carrental.service.auth.CustomUserDetails;
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
