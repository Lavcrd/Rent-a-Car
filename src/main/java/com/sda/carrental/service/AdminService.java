package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.enums.Role;
import com.sda.carrental.model.users.Admin;
import com.sda.carrental.repository.AdminRepository;
import com.sda.carrental.service.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepository repository;

    public Admin findById(Long id) throws ResourceNotFoundException {
        return repository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    public boolean hasAdminAuthority(CustomUserDetails cud) {
        try {
            return findById(cud.getId()).getRole().equals(Role.ROLE_ADMIN);
        } catch (Exception e) {
            return false;
        }
    }
}
